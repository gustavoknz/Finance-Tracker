package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.data.local.MetadataDao
import dev.gustavo.finance.data.local.MetadataEntity
import dev.gustavo.finance.data.local.PinDao
import dev.gustavo.finance.data.local.PinEntity
import dev.gustavo.finance.data.mapper.toDataError
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.domain.util.getOrNull
import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.MetricsCollector
import co.touchlab.kermit.Logger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Clock

class RealExchangeRateRepository(
    private val currencyService: CurrencyService,
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val metadataDao: MetadataDao,
    private val pinDao: PinDao,
    private val dispatchers: CoroutineDispatchers,
    private val metricsCollector: MetricsCollector
) : ExchangeRateRepository {

    private val logger = Logger.withTag("ExchangeRateRepository")
    private val repositoryScope = CoroutineScope(SupervisorJob() + dispatchers.io)

    companion object {
        private const val CURRENCIES_TTL = 24 * 60 * 60 * 1000L // 24 hours
        private const val RATES_TTL = 30 * 60 * 1000L // 30 minutes
        private const val CLEANUP_THRESHOLD = 7 * 24 * 60 * 60 * 1000L // 7 days
    }

    init {
        cleanupOldData()
    }

    private fun cleanupOldData() {
        repositoryScope.launch {
            try {
                logger.d { "Running periodic cache cleanup..." }
                val cleanupTime = Clock.System.now().toEpochMilliseconds() - CLEANUP_THRESHOLD
                exchangeRateDao.deleteOldRates(cleanupTime)
                currencyDao.deleteOldCurrencies(cleanupTime)
                logger.d { "Cache cleanup completed." }
            } catch (e: Exception) {
                logger.e(e) { "Failed to cleanup old cache" }
            }
        }
    }

    override fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>> = channelFlow {
        logger.d { "getLatestRates(base=$base)" }
        
        try {
            val cachedEntities = exchangeRateDao.getRatesByBaseOnce(base)
            val cachedResponse = if (cachedEntities.isNotEmpty()) {
                logger.d { "Found ${cachedEntities.size} cached rates for $base" }
                metricsCollector.trackCacheHit("rates_$base")
                ExchangeRatesResponse(
                    amount = 1.0,
                    base = base,
                    date = cachedEntities.first().date,
                    rates = cachedEntities.associate { it.targetCode to it.rate }
                )
            } else {
                logger.d { "No cached rates found for $base" }
                metricsCollector.trackCacheMiss("rates_$base")
                null
            }

            send(Result.Loading(cachedResponse))
        } catch (e: Throwable) {
            send(Result.Loading(null))
            send(Result.Error(e.toDataError()))
        }

        launch(dispatchers.io) {
            try {
                val lastUpdatedMillis = metadataDao.getLastUpdatedTimestamp("rates_$base")
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val isStale = if (lastUpdatedMillis == null) {
                    true
                } else {
                    (currentTimeMillis - lastUpdatedMillis) > RATES_TTL
                }

                if (isStale) {
                    logger.d { "Rates for $base are stale or missing, fetching from network..." }
                    metricsCollector.trackRefresh("rates_$base")
                    val remoteResponse = currencyService.getLatestRates(base)
                    val entities = remoteResponse.rates.map { (targetCode, rate) ->
                        ExchangeRateEntity(
                            baseCode = base,
                            targetCode = targetCode,
                            rate = rate,
                            date = remoteResponse.date,
                            localTimestamp = currentTimeMillis
                        )
                    }
                    exchangeRateDao.insertRates(entities)
                    metadataDao.insertMetadata(MetadataEntity("rates_$base", currentTimeMillis))
                    logger.d { "Successfully updated ${entities.size} rates for $base in database" }
                } else {
                    logger.d { "Rates for $base are still fresh (TTL)" }
                }
            } catch (e: Throwable) {
                logger.e(e) { "Error fetching latest rates for $base" }
                send(Result.Error(e.toDataError()))
            }
        }

        try {
            exchangeRateDao.getRatesByBase(base)
                .mapNotNull { entities ->
                    if (entities.isNotEmpty()) {
                        Result.Success(
                            ExchangeRatesResponse(
                                amount = 1.0,
                                base = base,
                                date = entities.first().date,
                                rates = entities.associate { it.targetCode to it.rate }
                            )
                        )
                    } else null
                }
                .collect { 
                    logger.d { "Emitting ${it.getOrNull()?.rates?.size ?: 0} rates for $base from DB flow" }
                    send(it) 
                }
        } catch (e: Throwable) {
            logger.e(e) { "Error observing rates for $base in DB" }
            send(Result.Error(e.toDataError()))
        }
    }.flowOn(dispatchers.io).distinctUntilChanged()

    override fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>> = channelFlow {
        logger.d { "getCurrencies()" }
        
        try {
            val cachedEntities = currencyDao.getAllCurrenciesOnce()
            val cachedMap = if (cachedEntities.isNotEmpty()) {
                logger.d { "Found ${cachedEntities.size} cached currencies" }
                metricsCollector.trackCacheHit("currencies")
                cachedEntities.associate { it.code to it.name }
            } else {
                logger.d { "No cached currencies found" }
                metricsCollector.trackCacheMiss("currencies")
                null
            }

            send(Result.Loading(cachedMap))
        } catch (e: Throwable) {
            send(Result.Loading(null))
            send(Result.Error(e.toDataError()))
        }

        launch(dispatchers.io) {
            try {
                val lastUpdatedMillis = metadataDao.getLastUpdatedTimestamp("currencies")
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val isStale = if (lastUpdatedMillis == null) {
                    true
                } else {
                    (currentTimeMillis - lastUpdatedMillis) > CURRENCIES_TTL
                }

                if (isStale) {
                    logger.d { "Currencies are stale or missing, fetching from network..." }
                    metricsCollector.trackRefresh("currencies")
                    val remoteCurrencies = currencyService.getCurrencies()
                    val entities = remoteCurrencies.map { (code, name) ->
                        CurrencyEntity(code = code, name = name, localTimestamp = currentTimeMillis)
                    }
                    currencyDao.insertCurrencies(entities)
                    metadataDao.insertMetadata(MetadataEntity("currencies", currentTimeMillis))
                    logger.d { "Successfully updated ${entities.size} currencies in database" }
                } else {
                    logger.d { "Currencies are still fresh (TTL)" }
                }
            } catch (e: Throwable) {
                logger.e(e) { "Error fetching currencies" }
                send(Result.Error(e.toDataError()))
            }
        }

        try {
            currencyDao.getAllCurrencies()
                .mapNotNull { entities ->
                    if (entities.isNotEmpty()) {
                        Result.Success(entities.associate { it.code to it.name })
                    } else null
                }
                .collect { 
                    logger.d { "Emitting ${it.getOrNull()?.size ?: 0} currencies from DB flow" }
                    send(it) 
                }
        } catch (e: Throwable) {
            logger.e(e) { "Error observing currencies in DB" }
            send(Result.Error(e.toDataError()))
        }
    }.flowOn(dispatchers.io).distinctUntilChanged()

    override fun getPinnedCurrencies(): Flow<Set<String>> =
        pinDao.getAllPinnedCodes()
            .map { 
                logger.d { "Found ${it.size} pinned codes in DB" }
                it.toSet() 
            }
            .flowOn(dispatchers.io)

    override suspend fun togglePin(code: String) = withContext(dispatchers.io) {
        logger.d { "togglePin(code=$code)" }
        val now = Clock.System.now().toEpochMilliseconds()
        if (pinDao.isPinned(code)) {
            logger.d { "Unpinning $code" }
            pinDao.deletePin(PinEntity(code, localTimestamp = now))
        } else {
            logger.d { "Pinning $code" }
            pinDao.insertPin(PinEntity(code, localTimestamp = now))
        }
    }
}
