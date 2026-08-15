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
        repository = RealExchangeRateRepository(service, currencyDao, exchangeRateDao, metadataDao, pinDao, dispatchers)
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
}
