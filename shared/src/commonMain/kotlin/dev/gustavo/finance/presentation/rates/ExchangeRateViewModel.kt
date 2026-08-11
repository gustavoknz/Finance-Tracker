package dev.gustavo.finance.presentation.rates

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.usecase.GetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.usecase.SetBaseCurrencyUseCase
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.util.getCurrencySymbol
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

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
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ExchangeRateState> = combine(
        _currentBase,
        _refreshTrigger.onStart { emit(Unit) }
    ) { base, _ -> base }
        .onEach { setBaseCurrencyUseCase(it) }
        .flatMapLatest { base ->
            combine(
                getCurrenciesUseCase(),
                getLatestRatesUseCase(base)
            ) { currenciesResult, ratesResult ->
                mapToState(base, currenciesResult, ratesResult)
            }
        }.scan(ExchangeRateState.Loading as ExchangeRateState) { previous, current ->
            if (current is ExchangeRateState.Loading && previous is ExchangeRateState.Success) {
                previous.copy(isRefreshing = true)
            } else {
                current
            }
        }
        .combine(_searchQuery) { currentState, query ->
            if (currentState is ExchangeRateState.Success && query.isNotBlank()) {
                currentState.copy(
                    rates = currentState.rates.filter {
                        it.code.contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true)
                    }.toImmutableList()
                )
            } else {
                currentState
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
            currenciesResult is Result.Success && ratesResult is Result.Success -> {
                createSuccessState(currenciesResult.data, ratesResult.data)
            }

            else -> ExchangeRateState.Loading
        }
    }

    private fun createSuccessState(
        currencyNames: Map<String, String>,
        ratesResponse: ExchangeRatesResponse
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
            lastUpdated = ratesResponse.date
        )
    }

    fun fetchRates(base: String) {
        _currentBase.value = base
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun refresh() {
        viewModelScope.launch {
            _refreshTrigger.emit(Unit)
        }
    }
}
