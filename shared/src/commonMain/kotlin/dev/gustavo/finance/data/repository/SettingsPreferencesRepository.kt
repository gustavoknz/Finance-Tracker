package dev.gustavo.finance.data.repository

import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import dev.gustavo.finance.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

@OptIn(ExperimentalSettingsApi::class)
class SettingsPreferencesRepository(
    private val settings: Settings
) : PreferencesRepository {

    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    companion object {
        private const val KEY_BASE_CURRENCY = "base_currency"
        private const val DEFAULT_BASE_CURRENCY = "EUR"
    }

    override fun getBaseCurrency(): String {
        return settings.getString(KEY_BASE_CURRENCY, DEFAULT_BASE_CURRENCY)
    }

    override fun getBaseCurrencyFlow(): Flow<String> {
        return observableSettings.getStringFlow(KEY_BASE_CURRENCY, DEFAULT_BASE_CURRENCY)
    }

    override fun setBaseCurrency(code: String) {
        settings[KEY_BASE_CURRENCY] = code
    }
}
