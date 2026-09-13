package dev.gustavo.finance.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakePreferencesRepository : PreferencesRepository {
    var storedBaseCurrency: String = "USD"
        set(value) {
            field = value
            _baseCurrencyFlow.value = value
        }

    private val _baseCurrencyFlow = MutableStateFlow(storedBaseCurrency)

    override fun getBaseCurrencyFlow(): Flow<String> = _baseCurrencyFlow

    override fun setBaseCurrency(code: String) {
        storedBaseCurrency = code
    }
}
