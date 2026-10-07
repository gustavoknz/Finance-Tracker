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
import dev.gustavo.finance.util.TimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RealExchangeRateRepository(
    private val currencyService: CurrencyService,
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val metadataDao: MetadataDao,
    private val pinDao: PinDao,
    private val dispatchers: CoroutineDispatchers,
    private val metricsCollector: MetricsCollector,
    private val timeProvider: TimeProvider,
    private val cacheConfig: CacheConfig = CacheConfig(),
    private val repositoryScope: CoroutineScope = CoroutineScope(SupervisorJob() + dispatchers.io),
) : ExchangeRateRepository {

    private val logger = Logger.withTag("ExchangeRateRepository")

    companion object {
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
                val cleanupTime = timeProvider.currentTimeMillis() - cacheConfig.cleanupThresholdMillis
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
        return managedNetworkResource(
            key = key,
            ttl = cacheConfig.ratesTtlMillis,
            queryOnce = { exchangeRateDao.getRatesByBaseOnce(base).toResponse(base) },
            queryFlow = { exchangeRateDao.getRatesByBase(base).mapNotNull { it.toResponse(base) } },
            fetch = { currencyService.getLatestRates(base) },
            saveFetchResult = { response, timestamp ->
                exchangeRateDao.insertRates(response.toEntities(timestamp))
            },
        )
    }

    override fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>> =
        managedNetworkResource(
            key = KEY_CURRENCIES,
            ttl = cacheConfig.currenciesTtlMillis,
            queryOnce = {
                val cached = currencyDao.getAllCurrenciesOnce()
                if (cached.isNotEmpty()) cached.toCurrencyMap() else null
            },
            queryFlow = {
                currencyDao.getAllCurrencies().mapNotNull { if (it.isNotEmpty()) it.toCurrencyMap() else null }
            },
            fetch = { currencyService.getCurrencies() },
            saveFetchResult = { response, timestamp ->
                currencyDao.insertCurrencies(response.toCurrencyEntities(timestamp))
            },
        )

    /**
     * Higher-level helper that automates metadata, metrics, and logging for resources.
     */
    private fun <ResultType, RequestType> managedNetworkResource(
        key: String,
        ttl: Long,
        queryOnce: suspend () -> ResultType?,
        queryFlow: () -> Flow<ResultType>,
        fetch: suspend () -> RequestType,
        saveFetchResult: suspend (RequestType, Long) -> Unit,
    ): Flow<Result<ResultType, DataError.Network>> = networkBoundResource(
        queryOnce = {
            val data = queryOnce()
            if (data != null) {
                logger.d { "Cache HIT for resource: $key" }
                metricsCollector.trackCacheHit(key)
            } else {
                logger.d { "Cache MISS for resource: $key" }
                metricsCollector.trackCacheMiss(key)
            }
            data
        },
        queryFlow = queryFlow,
        fetch = {
            logger.d { "Fetching fresh data for resource: $key" }
            metricsCollector.trackRefresh(key)
            fetch()
        },
        saveFetchResult = { response ->
            val timestamp = timeProvider.currentTimeMillis()
            saveFetchResult(response, timestamp)
            metadataDao.insertMetadata(MetadataEntity(key, timestamp))
            logger.d { "Successfully updated database and metadata for resource: $key" }
        },
        shouldFetch = { isCacheStale(key, ttl) },
        onFetchFailed = { logger.e(it) { "Network fetch failed for resource: $key" } },
        onQueryFailed = { logger.e(it) { "Database error for resource: $key" } },
    ).flowOn(dispatchers.io)

    private suspend fun isCacheStale(key: String, ttl: Long): Boolean = withContext(dispatchers.io) {
        val lastUpdatedMillis = metadataDao.getLastUpdatedTimestamp(key)
        val currentTimeMillis = timeProvider.currentTimeMillis()
        lastUpdatedMillis == null || (currentTimeMillis - lastUpdatedMillis) > ttl
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
        val now = timeProvider.currentTimeMillis()
        if (pinDao.isPinned(code)) {
            logger.d { "Unpinning $code" }
            pinDao.deletePin(PinEntity(code, localTimestamp = now))
        } else {
            logger.d { "Pinning $code" }
            pinDao.insertPin(PinEntity(code, localTimestamp = now))
        }
    }
}
