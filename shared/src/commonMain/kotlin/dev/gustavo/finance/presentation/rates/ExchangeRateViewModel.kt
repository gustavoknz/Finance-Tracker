package dev.gustavo.finance.presentation.rates

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.usecase.GetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.usecase.GetPinnedCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.SetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.TogglePinUseCase
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import dev.gustavo.finance.domain.util.getOrNull
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@Immutable
sealed interface ExchangeRateState {
    data object Loading : ExchangeRateState
    data class Success(
        val base: String,
        val pinnedRates: ImmutableList<ExchangeRateUiModel>,
        val otherRates: ImmutableList<ExchangeRateUiModel>,
        val lastUpdated: String,
        val isRefreshing: Boolean = false,
        val syncError: DataError.Network? = null
    ) : ExchangeRateState

    data class Error(val error: DataError.Network, val base: String) : ExchangeRateState
}

@Suppress("LongParameterList")
class ExchangeRateViewModel(
    private val getCurrenciesUseCase: GetCurrenciesUseCase,
    private val getLatestRatesUseCase: GetLatestRatesUseCase,
    private val setBaseCurrencyUseCase: SetBaseCurrencyUseCase,
    private val getPinnedCurrenciesUseCase: GetPinnedCurrenciesUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val displayMapper: ExchangeRateDisplayMapper,
    getBaseCurrencyUseCase: GetBaseCurrencyUseCase
) : ViewModel() {

    private val _currentBase = MutableStateFlow(getBaseCurrencyUseCase())
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _uiEvents = MutableSharedFlow<ExchangeRateUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ExchangeRateState> = combine(
        _currentBase,
        _refreshTrigger.onStart { emit(Unit) }
    ) { base, _ -> base }
        .onEach { setBaseCurrencyUseCase(it) }
        .flatMapLatest { base ->
            combine(
                getCurrenciesUseCase(),
                getLatestRatesUseCase(base),
                getPinnedCurrenciesUseCase()
            ) { currenciesResult, ratesResult, pinnedCodes ->
                mapToState(base, currenciesResult, ratesResult, pinnedCodes)
            }
        }.scan(ExchangeRateState.Loading as ExchangeRateState) { previous, current ->
            when {
                current is ExchangeRateState.Loading && previous is ExchangeRateState.Success -> {
                    previous.copy(isRefreshing = true, syncError = null)
                }

                current is ExchangeRateState.Error && previous is ExchangeRateState.Success -> {
                    // Non-blocking error: keep showing data but notify user
                    viewModelScope.launch {
                        _uiEvents.emit(ExchangeRateUiEvent.ShowOfflineNotification)
                    }
                    previous.copy(isRefreshing = false, syncError = current.error)
                }

                else -> current
            }
        }
        .combine(_searchQuery) { currentState, query ->
            if (currentState is ExchangeRateState.Success && query.isNotBlank()) {
                currentState.copy(
                    pinnedRates = currentState.pinnedRates.filter {
                        it.code.contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true)
                    }.toImmutableList(),
                    otherRates = currentState.otherRates.filter {
                        it.code.contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true)
                    }.toImmutableList()
                )
            } else {
                currentState
            }
        }
        .catch { _ ->
            emit(ExchangeRateState.Error(DataError.Network.UNKNOWN, _currentBase.value))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ExchangeRateState.Loading
        )

    private fun mapToState(
        base: String,
        currenciesResult: Result<Map<String, String>, DataError.Network>,
        ratesResult: Result<ExchangeRatesResponse, DataError.Network>,
        pinnedCodes: Set<String>
    ): ExchangeRateState {
        val currencies = currenciesResult.getOrNull()
        val rates = ratesResult.getOrNull()

        return when {
            currencies != null && rates != null -> {
                val successState = displayMapper.mapToSuccessState(currencies, rates, pinnedCodes)
                if (currenciesResult is Result.Loading || ratesResult is Result.Loading) {
                    successState.copy(isRefreshing = true)
                } else {
                    successState
                }
            }
            currenciesResult is Result.Error -> ExchangeRateState.Error(currenciesResult.error, base)
            ratesResult is Result.Error -> ExchangeRateState.Error(ratesResult.error, base)
            else -> ExchangeRateState.Loading
        }
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

    fun togglePin(code: String) {
        viewModelScope.launch {
            togglePinUseCase(code)
        }
    }
}

sealed interface ExchangeRateUiEvent {
    data object ShowOfflineNotification : ExchangeRateUiEvent
}
