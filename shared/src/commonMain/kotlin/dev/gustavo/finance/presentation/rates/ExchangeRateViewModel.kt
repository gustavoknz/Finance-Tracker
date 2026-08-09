package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.usecase.GetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.SetBaseCurrencyUseCase
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.util.getCurrencySymbol
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
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
    private val getLatestRatesUseCase: GetLatestRatesUseCase,
    private val setBaseCurrencyUseCase: SetBaseCurrencyUseCase,
    getBaseCurrencyUseCase: GetBaseCurrencyUseCase
) : ViewModel() {

    private var cachedCurrencyNames: Map<String, String> = emptyMap()
    private val namesMutex = Mutex()

    private val _currentBase = MutableStateFlow(getBaseCurrencyUseCase())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ExchangeRateState> = _currentBase
        .onEach { setBaseCurrencyUseCase(it) }
        .flatMapLatest { base ->
            flow {
                val previousState = this@ExchangeRateViewModel.state.value

                // 1. Handle Currency Names Loading
                if (cachedCurrencyNames.isEmpty()) {
                    emit(ExchangeRateState.Loading)
                    when (val result = getCurrencyNames()) {
                        is Result.Error -> {
                            emit(ExchangeRateState.Error(result.error, base))
                            return@flow
                        }

                        is Result.Success -> { /* Continue */
                        }
                    }
                } else if (previousState is ExchangeRateState.Success) {
                    // Show refresh indicator if we already have data
                    emit(previousState.copy(isRefreshing = true))
                }

                // 2. Handle Rates Fetching
                when (val result = getLatestRatesUseCase(base)) {
                    is Result.Error -> {
                        emit(ExchangeRateState.Error(result.error, base))
                    }

                    is Result.Success -> {
                        val response = result.data
                        val uiRates = response.rates.map { (code, rate) ->
                            ExchangeRateUiModel(
                                code = code,
                                name = cachedCurrencyNames[code] ?: "",
                                symbol = getCurrencySymbol(code),
                                rate = rate,
                                formattedRate = rate.toString()
                            )
                        }.toImmutableList()

                        emit(
                            ExchangeRateState.Success(
                                base = response.base,
                                rates = uiRates,
                                lastUpdated = response.date,
                                isRefreshing = false
                            )
                        )
                    }
                }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExchangeRateState.Loading
        )

    private suspend fun getCurrencyNames(): Result<Map<String, String>, DataError.Network> {
        return namesMutex.withLock {
            if (cachedCurrencyNames.isNotEmpty()) return@withLock Result.Success(cachedCurrencyNames)
            val result = getCurrenciesUseCase()
            if (result is Result.Success) {
                cachedCurrencyNames = result.data
            }
            result
        }
    }

    fun fetchRates(base: String) {
        _currentBase.value = base
    }
}
