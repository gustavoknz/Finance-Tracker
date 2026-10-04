package dev.gustavo.finance.data.repository

import com.russhwolf.settings.MapSettings
import dev.gustavo.finance.util.CoroutineDispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsPreferencesRepositoryTest {

    private lateinit var settings: MapSettings
    private lateinit var repository: SettingsPreferencesRepository

    private val testDispatcher = UnconfinedTestDispatcher()
    private val dispatchers = CoroutineDispatchers(
        main = testDispatcher,
        default = testDispatcher,
        io = testDispatcher,
    )

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        repository = SettingsPreferencesRepository(settings, dispatchers)
    }

    @Test
    fun `getBaseCurrencyFlow should emit default when not set`() = runTest {
        assertEquals("EUR", repository.getBaseCurrencyFlow().first())
    }

    @Test
    fun `getBaseCurrencyFlow should emit saved value`() = runTest {
        repository.setBaseCurrency("USD")
        assertEquals("USD", repository.getBaseCurrencyFlow().first())
    }

    @Test
    fun `setBaseCurrency should update settings`() {
        repository.setBaseCurrency("BRL")
        assertEquals("BRL", settings.getString("base_currency", ""))
    }
}
