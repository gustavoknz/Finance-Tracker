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
import dev.gustavo.finance.util.FakeTimeProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
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
        io = testDispatcher,
    )
    private val testScope = CoroutineScope(SupervisorJob() + testDispatcher)
    private val timeProvider = FakeTimeProvider(1000L)
    private val cacheConfig = CacheConfig()

    @BeforeTest
    fun setUp() {
        service = FakeCurrencyService()
        currencyDao = FakeCurrencyDao()
        exchangeRateDao = FakeExchangeRateDao()
        metadataDao = FakeMetadataDao()
        pinDao = FakePinDao()
        metricsCollector = FakeMetricsCollector()
        repository = RealExchangeRateRepository(
            service,
            currencyDao,
            exchangeRateDao,
            metadataDao,
            pinDao,
            dispatchers,
            metricsCollector,
            timeProvider,
            cacheConfig,
            testScope,
        )
    }

    @Test
    fun `getLatestRates should emit loading and then data from network`() = runTest {
        val base = "USD"
        val expectedResponse = ExchangeRatesResponse(1.0, base, "2024-05-20", mapOf("EUR" to 0.92))
        service.latestRatesResult = expectedResponse

        val results = repository.getLatestRates(base).take(2).toList()

        assertTrue(results[0] is Result.Loading)
        val success = results[1] as Result.Success
        assertEquals(expectedResponse, success.data)
    }

    @Test
    fun `getCurrencies should emit loading and then data from network`() = runTest {
        val expectedResponse = mapOf("USD" to "Dollar", "EUR" to "Euro")
        service.currenciesResult = expectedResponse

        val results = repository.getCurrencies().take(2).toList()

        assertTrue(results[0] is Result.Loading)
        val success = results[1] as Result.Success
        assertEquals(expectedResponse, success.data)
    }

    @Test
    fun `getPinnedCurrencies should emit pinned codes from dao`() = runTest {
        pinDao.insertPin(dev.gustavo.finance.data.local.PinEntity("USD"))
        pinDao.insertPin(dev.gustavo.finance.data.local.PinEntity("EUR"))

        val pinned = repository.getPinnedCurrencies().first()
        assertEquals(setOf("USD", "EUR"), pinned)
    }

    @Test
    fun `togglePin should insert if not pinned and delete if pinned`() = runTest {
        val code = "USD"
        // Initially not pinned
        assertEquals(false, pinDao.isPinned(code))

        // Toggle to pin
        repository.togglePin(code)
        assertEquals(true, pinDao.isPinned(code))

        // Toggle to unpin
        repository.togglePin(code)
        assertEquals(false, pinDao.isPinned(code))
    }

    @Test
    fun `cleanupOldData should handle errors gracefully`() = runTest {
        exchangeRateDao.shouldThrow = true
        // Just create a new repository to trigger init block with error
        RealExchangeRateRepository(service, currencyDao, exchangeRateDao, metadataDao, pinDao, dispatchers, metricsCollector, timeProvider, cacheConfig, testScope)
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
        val now = 1000L
        timeProvider.currentTime = now
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("rates_$base", now))
        exchangeRateDao.insertRates(listOf(ExchangeRateEntity(base, "EUR", 0.92, "2024-05-20")))

        service.shouldThrow = true // Should not be called

        val results = repository.getLatestRates(base).take(2).toList()
        assertTrue(results.any { it is Result.Success })
    }

    @Test
    fun `getCurrencies should not refresh if data is fresh`() = runTest {
        val now = 1000L
        timeProvider.currentTime = now
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", now))
        currencyDao.insertCurrencies(listOf(dev.gustavo.finance.data.local.CurrencyEntity("USD", "Dollar")))

        service.shouldThrow = true // Should not be called

        val results = repository.getCurrencies().take(2).toList()
        assertTrue(results.any { it is Result.Success })
    }

    @Test
    fun `getCurrencies should refresh when cache is stale according to FakeTimeProvider`() = runTest {
        val initialTime = 1000L
        timeProvider.currentTime = initialTime
        metadataDao.insertMetadata(dev.gustavo.finance.data.local.MetadataEntity("currencies", initialTime))
        currencyDao.insertCurrencies(listOf(dev.gustavo.finance.data.local.CurrencyEntity("USD", "Dollar")))

        // Advance time beyond TTL (24 hours + 1 ms)
        timeProvider.advanceTime(24 * 60 * 60 * 1000L + 1L)

        val expectedResponse = mapOf("EUR" to "Euro", "USD" to "Dollar")
        service.currenciesResult = expectedResponse

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
