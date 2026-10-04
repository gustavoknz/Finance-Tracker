package dev.gustavo.finance.data.repository

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.coroutines.getStringFlow
import com.russhwolf.settings.set
import dev.gustavo.finance.domain.repository.PreferencesRepository
import dev.gustavo.finance.util.CoroutineDispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn

class SettingsPreferencesRepository(
    private val settings: Settings,
    private val dispatchers: CoroutineDispatchers,
) : PreferencesRepository {

    private val observableSettings: ObservableSettings by lazy { settings as ObservableSettings }

    companion object {
        private const val KEY_BASE_CURRENCY = "base_currency"
        private const val DEFAULT_BASE_CURRENCY = "EUR"
    }

    override fun getBaseCurrencyFlow(): Flow<String> {
        return observableSettings.getStringFlow(KEY_BASE_CURRENCY, DEFAULT_BASE_CURRENCY)
            .flowOn(dispatchers.io)
    }

    override fun setBaseCurrency(code: String) {
        settings[KEY_BASE_CURRENCY] = code
    }
}
