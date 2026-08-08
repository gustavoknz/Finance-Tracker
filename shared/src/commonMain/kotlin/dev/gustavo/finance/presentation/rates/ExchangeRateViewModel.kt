package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.domain.util.onError
import dev.gustavo.finance.domain.util.onSuccess
import dev.gustavo.finance.domain.util.map
import dev.gustavo.finance.util.getCurrencySymbol
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList

sealed interface ExchangeRateState {
    data object Loading : ExchangeRateState
    data class Success(
        val base: String,
        val rates: ImmutableList<ExchangeRateUiModel>,
        val lastUpdated: String,
        val isRefreshing: Boolean = false
    ) : ExchangeRateState
    data class Error(val error: DataError.Network, val base: String) : ExchangeRateState
}

class ExchangeRateViewModel(
    private val getCurrenciesUseCase: GetCurrenciesUseCase,
    private val getLatestRatesUseCase: GetLatestRatesUseCase
) : ViewModel() {
    private val _state = MutableStateFlow<ExchangeRateState>(ExchangeRateState.Loading)
    val state: StateFlow<ExchangeRateState> = _state.asStateFlow()
    
    private var cachedCurrencyNames: Map<String, String> = emptyMap()
    private val namesMutex = Mutex()

    init {
        fetchRates("EUR")
    }

    private suspend fun ensureNamesLoaded(): Result<Unit, DataError.Network> {
        return namesMutex.withLock {
            if (cachedCurrencyNames.isNotEmpty()) return@withLock Result.Success(Unit)
            getCurrenciesUseCase()
                .onSuccess { cachedCurrencyNames = it }
                .map { Unit }
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
            
            ensureNamesLoaded()
                .onSuccess {
                    getLatestRatesUseCase(base)
                        .onSuccess { response ->
                            val uiRates = response.rates.map { (code, rate) ->
                                ExchangeRateUiModel(
                                    code = code,
                                    name = cachedCurrencyNames[code] ?: "",
                                    symbol = getCurrencySymbol(code),
                                    rate = rate,
                                    formattedRate = rate.toString()
                                )
                            }.toImmutableList()
                            _state.value = ExchangeRateState.Success(
                                base = response.base,
                                rates = uiRates,
                                lastUpdated = response.date
                            )
                        }
                        .onError { error ->
                            _state.value = ExchangeRateState.Error(error, base)
                        }
                }
                .onError { error ->
                    _state.value = ExchangeRateState.Error(error, base)
                }
        }
    }
}
