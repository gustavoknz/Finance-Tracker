package dev.gustavo.finance.data.repository

import co.touchlab.kermit.Logger
import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.MetadataDao
import dev.gustavo.finance.data.local.MetadataEntity
import dev.gustavo.finance.data.local.PinDao
import dev.gustavo.finance.data.local.PinEntity
import dev.gustavo.finance.data.mapper.toCurrencyEntities
import dev.gustavo.finance.data.mapper.toCurrencyMap
import dev.gustavo.finance.data.mapper.toDataError
import dev.gustavo.finance.data.mapper.toEntities
import dev.gustavo.finance.data.mapper.toResponse
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.data.util.networkBoundResource
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.MetricsCollector
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
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

        private const val KEY_CURRENCIES = "currencies"
        private fun ratesKey(base: String) = "rates_$base"
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

    override fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>> {
        val key = ratesKey(base)
        return networkBoundResource(
            queryOnce = {
                val cachedEntities = exchangeRateDao.getRatesByBaseOnce(base)
                val cachedResponse = cachedEntities.toResponse(base)
                if (cachedResponse != null) {
                    logger.d { "Found ${cachedEntities.size} cached rates for $base" }
                    metricsCollector.trackCacheHit(key)
                } else {
                    logger.d { "No cached rates found for $base" }
                    metricsCollector.trackCacheMiss(key)
                }
                cachedResponse
            },
            queryFlow = {
                exchangeRateDao.getRatesByBase(base)
                    .mapNotNull { it.toResponse(base) }
            },
            fetch = {
                logger.d { "Rates for $base are stale or missing, fetching from network..." }
                metricsCollector.trackRefresh(key)
                currencyService.getLatestRates(base)
            },
            saveFetchResult = { remoteResponse ->
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val entities = remoteResponse.toEntities(currentTimeMillis)
                exchangeRateDao.insertRates(entities)
                metadataDao.insertMetadata(MetadataEntity(key, currentTimeMillis))
                logger.d { "Successfully updated ${entities.size} rates for $base in database" }
            },
            shouldFetch = { isCacheStale(key, RATES_TTL) },
            onFetchFailed = { logger.e(it) { "Error fetching latest rates for $base" } },
            onQueryFailed = { logger.e(it) { "Error observing rates for $base in DB" } }
        ).flowOn(dispatchers.io)
    }

    override fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>> =
        networkBoundResource(
            queryOnce = {
                val cachedEntities = currencyDao.getAllCurrenciesOnce()
                if (cachedEntities.isNotEmpty()) {
                    logger.d { "Found ${cachedEntities.size} cached currencies" }
                    metricsCollector.trackCacheHit(KEY_CURRENCIES)
                    cachedEntities.toCurrencyMap()
                } else {
                    logger.d { "No cached currencies found" }
                    metricsCollector.trackCacheMiss(KEY_CURRENCIES)
                    null
                }
            },
            queryFlow = {
                currencyDao.getAllCurrencies()
                    .mapNotNull { if (it.isNotEmpty()) it.toCurrencyMap() else null }
            },
            fetch = {
                logger.d { "Currencies are stale or missing, fetching from network..." }
                metricsCollector.trackRefresh(KEY_CURRENCIES)
                currencyService.getCurrencies()
            },
            saveFetchResult = { remoteCurrencies ->
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val entities = remoteCurrencies.toCurrencyEntities(currentTimeMillis)
                currencyDao.insertCurrencies(entities)
                metadataDao.insertMetadata(MetadataEntity(KEY_CURRENCIES, currentTimeMillis))
                logger.d { "Successfully updated ${entities.size} currencies in database" }
            },
            shouldFetch = { isCacheStale(KEY_CURRENCIES, CURRENCIES_TTL) },
            onFetchFailed = { logger.e(it) { "Error fetching currencies" } },
            onQueryFailed = { logger.e(it) { "Error observing currencies in DB" } }
        ).flowOn(dispatchers.io)

    private suspend fun isCacheStale(key: String, ttl: Long): Boolean {
        val lastUpdatedMillis = metadataDao.getLastUpdatedTimestamp(key)
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        return if (lastUpdatedMillis == null) {
            true
        } else {
            (currentTimeMillis - lastUpdatedMillis) > ttl
        }
    }

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
