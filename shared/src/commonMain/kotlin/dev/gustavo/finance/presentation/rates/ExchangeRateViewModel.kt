package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface ExchangeRateState {
    data object Loading : ExchangeRateState
    data class Success(val base: String, val rates: Map<String, Double>) : ExchangeRateState
    data class Error(val message: String) : ExchangeRateState
}

class ExchangeRateViewModel(
    private val repository: ExchangeRateRepository
) : ViewModel() {
    private val _state = MutableStateFlow<ExchangeRateState>(ExchangeRateState.Loading)
    val state: StateFlow<ExchangeRateState> = _state.asStateFlow()

    init {
        fetchRates("EUR")
    }

    fun fetchRates(base: String) {
        viewModelScope.launch {
            _state.value = ExchangeRateState.Loading
            try {
                val response = repository.getLatestRates(base)
                _state.value = ExchangeRateState.Success(response.base, response.rates)
            } catch (e: Exception) {
                _state.value = ExchangeRateState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
