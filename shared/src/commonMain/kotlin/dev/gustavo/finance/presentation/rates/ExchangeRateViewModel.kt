package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
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

    private val _currentBase = MutableStateFlow(getBaseCurrencyUseCase())

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ExchangeRateState> = _currentBase
        .onEach { setBaseCurrencyUseCase(it) }
        .flatMapLatest { base ->
            combine(
                getCurrenciesUseCase(),
                getLatestRatesUseCase(base)
            ) { currenciesResult, ratesResult ->
                mapToState(base, currenciesResult, ratesResult)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExchangeRateState.Loading
        )

    private fun mapToState(
        base: String,
        currenciesResult: Result<Map<String, String>, DataError.Network>,
        ratesResult: Result<ExchangeRatesResponse, DataError.Network>
    ): ExchangeRateState {
        return when {
            currenciesResult is Result.Error -> ExchangeRateState.Error(currenciesResult.error, base)
            ratesResult is Result.Error -> ExchangeRateState.Error(ratesResult.error, base)
            currenciesResult is Result.Loading || ratesResult is Result.Loading -> {
                if (currenciesResult is Result.Success && ratesResult is Result.Success) {
                    createSuccessState(base, currenciesResult.data, ratesResult.data, isRefreshing = true)
                } else {
                    ExchangeRateState.Loading
                }
            }
            currenciesResult is Result.Success && ratesResult is Result.Success -> {
                createSuccessState(base, currenciesResult.data, ratesResult.data, isRefreshing = false)
            }
            else -> ExchangeRateState.Loading
        }
    }

    private fun createSuccessState(
        base: String,
        currencyNames: Map<String, String>,
        ratesResponse: ExchangeRatesResponse,
        isRefreshing: Boolean
    ): ExchangeRateState.Success {
        val uiRates = ratesResponse.rates.map { (code, rate) ->
            ExchangeRateUiModel(
                code = code,
                name = currencyNames[code] ?: "",
                symbol = getCurrencySymbol(code),
                rate = rate,
                formattedRate = rate.toString()
            )
        }.toImmutableList()

        return ExchangeRateState.Success(
            base = ratesResponse.base,
            rates = uiRates,
            lastUpdated = ratesResponse.date,
            isRefreshing = isRefreshing
        )
    }

    fun fetchRates(base: String) {
        _currentBase.value = base
    }
}
