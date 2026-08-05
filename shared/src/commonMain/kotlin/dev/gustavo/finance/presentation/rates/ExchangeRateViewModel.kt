package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.util.DataError
import dev.gustavo.finance.util.toDataError
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExchangeRateState {
    data object Loading : ExchangeRateState
    data class Success(
        val base: String,
        val rates: Map<String, Double>,
        val currencyNames: Map<String, String>,
        val lastUpdated: String,
        val isRefreshing: Boolean = false
    ) : ExchangeRateState
    data class Error(val error: DataError.Network, val base: String) : ExchangeRateState
}

class ExchangeRateViewModel(
    private val repository: ExchangeRateRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ExchangeRateState>(ExchangeRateState.Loading)
    val state: StateFlow<ExchangeRateState> = _state.asStateFlow()
    
    private var cachedCurrencyNames: Map<String, String> = emptyMap()

    init {
        fetchInitialData()
    }

    private fun fetchInitialData() {
        viewModelScope.launch {
            try {
                cachedCurrencyNames = repository.getCurrencies()
                fetchRates("EUR")
            } catch (e: Exception) {
                _state.value = ExchangeRateState.Error(e.toDataError(), "EUR")
            }
        }
    }

    fun fetchRates(base: String) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState is ExchangeRateState.Success) {
                _state.value = currentState.copy(isRefreshing = true)
            } else {
                _state.value = ExchangeRateState.Loading
            }
            try {
                val response = repository.getLatestRates(base)
                _state.value = ExchangeRateState.Success(
                    base = response.base,
                    rates = response.rates,
                    currencyNames = cachedCurrencyNames,
                    lastUpdated = response.date
                )
            } catch (e: Exception) {
                _state.value = ExchangeRateState.Error(e.toDataError(), base)
            }
        }
    }
}
