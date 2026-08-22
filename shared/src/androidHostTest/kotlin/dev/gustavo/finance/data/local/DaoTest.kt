package dev.gustavo.finance.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class DaoTest {

    private lateinit var database: AppDatabase
    private lateinit var currencyDao: CurrencyDao
    private lateinit var exchangeRateDao: ExchangeRateDao
    private lateinit var metadataDao: MetadataDao
    private lateinit var pinDao: PinDao

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            AppDatabase::class.java
        ).build()
        
        currencyDao = database.currencyDao()
        exchangeRateDao = database.exchangeRateDao()
        metadataDao = database.metadataDao()
        pinDao = database.pinDao()
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun insertAndGetAllCurrencies() = runTest {
        val currencies = listOf(
            CurrencyEntity("USD", "United States Dollar"),
            CurrencyEntity("EUR", "Euro")
        )
        currencyDao.insertCurrencies(currencies)

        val result = currencyDao.getAllCurrencies().first()
        assertEquals(2, result.size)
        assertTrue(result.any { it.code == "USD" && it.name == "United States Dollar" })
        assertTrue(result.any { it.code == "EUR" && it.name == "Euro" })
    }

    @Test
    fun insertAndGetRatesByBase() = runTest {
        val rates = listOf(
            ExchangeRateEntity("EUR", "USD", 1.08, "2024-05-20"),
            ExchangeRateEntity("EUR", "GBP", 0.85, "2024-05-20")
        )
        exchangeRateDao.insertRates(rates)

        val result = exchangeRateDao.getRatesByBase("EUR").first()
        assertEquals(2, result.size)
        assertTrue(result.any { it.targetCode == "USD" && it.rate == 1.08 })
    }

    @Test
    fun metadataTest() = runTest {
        val key = "test_key"
        val timestamp = 123456789L
        metadataDao.insertMetadata(MetadataEntity(key, timestamp))

        val result = metadataDao.getLastUpdatedTimestamp(key)
        assertEquals(timestamp, result)
    }

    @Test
    fun pinTest() = runTest {
        val code = "USD"
        pinDao.insertPin(PinEntity(code))

        assertTrue(pinDao.isPinned(code))
        val pinnedCodes = pinDao.getAllPinnedCodes().first()
        assertEquals(1, pinnedCodes.size)
        assertEquals(code, pinnedCodes.first())

        pinDao.deletePin(PinEntity(code))
        assertTrue(!pinDao.isPinned(code))
    }
}
