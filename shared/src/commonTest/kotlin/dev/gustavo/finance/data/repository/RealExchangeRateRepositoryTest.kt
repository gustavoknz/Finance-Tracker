package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.fake.FakeCurrencyDao
import dev.gustavo.finance.data.fake.FakeCurrencyService
import dev.gustavo.finance.data.fake.FakeExchangeRateDao
import dev.gustavo.finance.data.fake.FakeMetadataDao
import dev.gustavo.finance.data.fake.FakePinDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.FakeMetricsCollector
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RealExchangeRateRepositoryTest {

    private lateinit var service: FakeCurrencyService
    private lateinit var currencyDao: FakeCurrencyDao
    private lateinit var exchangeRateDao: FakeExchangeRateDao
    private lateinit var metadataDao: FakeMetadataDao
    private lateinit var pinDao: FakePinDao
    private lateinit var metricsCollector: FakeMetricsCollector
    private lateinit var repository: RealExchangeRateRepository
    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        default = testDispatcher,
        io = testDispatcher
    )

    @BeforeTest
    fun setUp() {
        service = FakeCurrencyService()
        currencyDao = FakeCurrencyDao()
        exchangeRateDao = FakeExchangeRateDao()
        metadataDao = FakeMetadataDao()
        pinDao = FakePinDao()
        metricsCollector = FakeMetricsCollector()
        repository = RealExchangeRateRepository(service, currencyDao, exchangeRateDao, metadataDao, pinDao, dispatchers, metricsCollector)
    }

    @Test
    fun `getLatestRates should emit loading and then data from network`() = runTest {
        val base = "USD"
        val expectedResponse = ExchangeRatesResponse(1.0, base, "2024-05-20", mapOf("EUR" to 0.92))
        service.latestRatesResult = expectedResponse

        val results = repository.getLatestRates(base).take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(expectedResponse, (results[1] as Result.Success).data)
    }

    @Test
    fun `getLatestRates should refresh stale cache with network data`() = runTest {
        val base = "USD"
        val staleTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds() - 31 * 60 * 1000L
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("rates_$base", staleTimestamp))
        exchangeRateDao.insertRates(
            listOf(
                ExchangeRateEntity(
                    base,
                    "EUR",
                    0.91,
                    "2024-05-19",
                    localTimestamp = staleTimestamp
                )
            )
        )

        val expectedResponse = ExchangeRatesResponse(1.0, base, "2024-05-20", mapOf("EUR" to 0.92, "GBP" to 0.78))
        service.latestRatesResult = expectedResponse

        val results = repository.getLatestRates(base).take(3).toList()
        val successResults = results.filterIsInstance<Result.Success<ExchangeRatesResponse>>()

        assertTrue(results.first() is Result.Loading)
        assertTrue(successResults.isNotEmpty())
        assertEquals(expectedResponse, successResults.last().data)
        assertTrue(metadataDao.getLastUpdatedTimestamp("rates_$base")!! >= staleTimestamp)
    }

    @Test
    fun `getLatestRates should emit data from cache if network fails`() = runTest {
        val base = "USD"
        val cachedRate = ExchangeRateEntity(base, "EUR", 0.92, "2024-05-19")
        exchangeRateDao.insertRates(listOf(cachedRate))
        service.shouldThrow = true

        val results = repository.getLatestRates(base).take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(0.92, (results[1] as Result.Success).data.rates["EUR"])
    }

    @Test
    fun `getCurrencies should fetch from network if stale`() = runTest {
        val expectedCurrencies = mapOf("USD" to "Dollar", "EUR" to "Euro")
        service.currenciesResult = expectedCurrencies

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(expectedCurrencies, (results[1] as Result.Success).data)
    }

    @Test
    fun `getCurrencies should refresh stale cache with fresh network data`() = runTest {
        val staleTimestamp = kotlin.time.Clock.System.now().toEpochMilliseconds() - 25 * 60 * 60 * 1000L
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", staleTimestamp))
        currencyDao.insertCurrencies(
            listOf(
                dev.gustavo.finance.data.local.CurrencyEntity(
                    "USD",
                    "Dollar",
                    localTimestamp = staleTimestamp
                )
            )
        )

        val expectedCurrencies = mapOf("USD" to "Dollar", "EUR" to "Euro")
        service.currenciesResult = expectedCurrencies

        val results = repository.getCurrencies().take(3).toList()
        val successResults = results.filterIsInstance<Result.Success<Map<String, String>>>()

        assertTrue(results.first() is Result.Loading)
        assertTrue(successResults.isNotEmpty())
        assertEquals(expectedCurrencies, successResults.last().data)
        assertTrue(metadataDao.getLastUpdatedTimestamp("currencies")!! >= staleTimestamp)
    }

    @Test
    fun `getCurrencies should return from cache if not stale`() = runTest {
        val cachedCurrencies = mapOf("USD" to "Dollar")
        currencyDao.insertCurrencies(listOf(dev.gustavo.finance.data.local.CurrencyEntity("USD", "Dollar")))

        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", now))

        service.shouldThrow = true // Should not be called

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(cachedCurrencies, (results[1] as Result.Success).data)
    }

    @Test
    fun `getLatestRates should emit error if network fails and no cache`() = runTest {
        val base = "USD"
        service.shouldThrow = true

        val results = repository.getLatestRates(base).take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Error)
    }

    @Test
    fun `getCurrencies should return from cache if network fails`() = runTest {
        val cachedCurrencies = mapOf("USD" to "Dollar")
        currencyDao.insertCurrencies(listOf(dev.gustavo.finance.data.local.CurrencyEntity("USD", "Dollar")))
        
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", 0L)) // Stale
        service.shouldThrow = true

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(cachedCurrencies, (results[1] as Result.Success).data)
    }

    @Test
    fun `getCurrencies should emit error if network fails and no cache`() = runTest {
        service.shouldThrow = true

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Error)
    }

    @Test
    fun `metrics should track hits misses and refreshes correctly`() = runTest {
        val base = "USD"
        
        // 1. Cache Miss + Refresh
        service.latestRatesResult = ExchangeRatesResponse(1.0, base, "2024-05-20", mapOf("EUR" to 0.92))
        repository.getLatestRates(base).take(2).toList()
        
        assertEquals(1, metricsCollector.cacheMissCount)
        assertEquals(1, metricsCollector.refreshCount)
        
        // 2. Cache Hit (Fresh data)
        repository.getLatestRates(base).take(1).toList()
        assertEquals(1, metricsCollector.cacheHitCount)
    }

    @Test
    fun `getLatestRates should handle DB flow error gracefully`() = runTest {
        val base = "USD"
        exchangeRateDao.shouldThrow = true

        val results = repository.getLatestRates(base).take(2).toList()

        assertTrue(results[1] is Result.Error)
    }

    @Test
    fun `getCurrencies should handle DB flow error gracefully`() = runTest {
        currencyDao.shouldThrow = true

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[1] is Result.Error)
    }

    @Test
    fun `getPinnedCurrencies should return set of pinned codes`() = runTest {
        pinDao.insertPin(dev.gustavo.finance.data.local.PinEntity("USD"))
        pinDao.insertPin(dev.gustavo.finance.data.local.PinEntity("EUR"))

        val pinned = repository.getPinnedCurrencies().take(1).toList().first()

        assertEquals(setOf("USD", "EUR"), pinned)
    }

    @Test
    fun `togglePin should add pin if not present`() = runTest {
        val code = "USD"
        repository.togglePin(code)
        assertTrue(pinDao.isPinned(code))
    }

    @Test
    fun `togglePin should remove pin if already present`() = runTest {
        val code = "USD"
        pinDao.insertPin(dev.gustavo.finance.data.local.PinEntity(code))
        repository.togglePin(code)
        assertTrue(!pinDao.isPinned(code))
    }

    @Test
    fun `cleanupOldData should handle errors gracefully`() = runTest {
        exchangeRateDao.shouldThrow = true
        // Just create a new repository to trigger init block with error
        RealExchangeRateRepository(service, currencyDao, exchangeRateDao, metadataDao, pinDao, dispatchers, metricsCollector)
        // If it doesn't crash, it's handled (verified by logs in real app)
    }

    @Test
    fun `getLatestRates should handle background refresh error gracefully`() = runTest {
        val base = "USD"
        metadataDao.shouldThrow = true // Trigger error in launch block

        val results = repository.getLatestRates(base).take(2).toList()
        
        // Should still show loading (and potentially error from send if caught)
        // The background error sends Result.Error
        assertTrue(results.any { it is Result.Error })
    }

    @Test
    fun `getCurrencies should handle background refresh error gracefully`() = runTest {
        metadataDao.shouldThrow = true

        val results = repository.getCurrencies().take(2).toList()
        assertTrue(results.any { it is Result.Error })
    }

    @Test
    fun `getLatestRates should not refresh if data is fresh`() = runTest {
        val base = "USD"
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("rates_$base", now))
        exchangeRateDao.insertRates(listOf(ExchangeRateEntity(base, "EUR", 0.92, "2024-05-20")))

        service.shouldThrow = true // Should not be called

        val results = repository.getLatestRates(base).take(2).toList()
        assertTrue(results.any { it is Result.Success })
    }

    @Test
    fun `getCurrencies should not refresh if data is fresh`() = runTest {
        val now = kotlin.time.Clock.System.now().toEpochMilliseconds()
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", now))
        currencyDao.insertCurrencies(listOf(dev.gustavo.finance.data.local.CurrencyEntity("USD", "Dollar")))

        service.shouldThrow = true // Should not be called

        val results = repository.getCurrencies().take(2).toList()
        assertTrue(results.any { it is Result.Success })
    }

    @Test
    fun `getLatestRates should not emit success if DB flow is empty`() = runTest {
        val base = "USD"
        service.shouldThrow = true // Prevent network success

        val results = repository.getLatestRates(base).take(2).toList()

        // Should only have Loading and Error, no Success
        assertTrue(results.none { it is Result.Success })
    }

    @Test
    fun `getCurrencies should not emit success if DB flow is empty`() = runTest {
        service.shouldThrow = true

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results.none { it is Result.Success })
    }
}
