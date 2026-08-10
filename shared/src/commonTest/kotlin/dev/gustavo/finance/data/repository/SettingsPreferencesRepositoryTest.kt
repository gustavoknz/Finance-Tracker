package dev.gustavo.finance.data.repository

import com.russhwolf.settings.MapSettings
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class SettingsPreferencesRepositoryTest {

    private lateinit var settings: MapSettings
    private lateinit var repository: SettingsPreferencesRepository

    @BeforeTest
    fun setUp() {
        settings = MapSettings()
        repository = SettingsPreferencesRepository(settings)
    }

    @Test
    fun `getBaseCurrency should return default when not set`() {
        assertEquals("EUR", repository.getBaseCurrency())
    }

    @Test
    fun `getBaseCurrency should return saved value`() {
        repository.setBaseCurrency("USD")
        assertEquals("USD", repository.getBaseCurrency())
    }

    @Test
    fun `setBaseCurrency should update settings`() {
        repository.setBaseCurrency("BRL")
        assertEquals("BRL", settings.getString("base_currency", ""))
    }
}
