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
import dev.gustavo.finance.util.CoroutineDispatchers
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

@Immutable
data class ExchangeRateUiState(
    val base: String,
    val searchQuery: String = "",
    val content: ExchangeRateState = ExchangeRateState.Loading
)

@Immutable
sealed interface ExchangeRateState {
    data object Loading : ExchangeRateState
    data class Success(
        val pinnedRates: ImmutableList<ExchangeRateUiModel>,
        val otherRates: ImmutableList<ExchangeRateUiModel>,
        val lastUpdated: String,
        val isRefreshing: Boolean = false,
        val syncError: DataError.Network? = null
    ) : ExchangeRateState

    data class Error(val error: DataError.Network) : ExchangeRateState
}

sealed interface ExchangeRateAction {
    data class ChangeBaseCurrency(val code: String) : ExchangeRateAction
    data class SearchQueryChanged(val query: String) : ExchangeRateAction
    data object Refresh : ExchangeRateAction
    data class TogglePin(val code: String) : ExchangeRateAction
}

@Suppress("LongParameterList")
class ExchangeRateViewModel(
    private val getCurrenciesUseCase: GetCurrenciesUseCase,
    private val getLatestRatesUseCase: GetLatestRatesUseCase,
    private val setBaseCurrencyUseCase: SetBaseCurrencyUseCase,
    private val getPinnedCurrenciesUseCase: GetPinnedCurrenciesUseCase,
    private val togglePinUseCase: TogglePinUseCase,
    private val displayMapper: ExchangeRateDisplayMapper,
    private val dispatchers: CoroutineDispatchers,
    getBaseCurrencyUseCase: GetBaseCurrencyUseCase
) : ViewModel() {

    private val _currentBase = MutableStateFlow(getBaseCurrencyUseCase())
    private val _refreshTrigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    private val _searchQuery = MutableStateFlow("")

    private val _uiEvents = MutableSharedFlow<ExchangeRateUiEvent>()
    val uiEvents = _uiEvents.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val contentState: Flow<ExchangeRateState> = combine(
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
                withContext(dispatchers.default) {
                    mapToContentState(currenciesResult, ratesResult, pinnedCodes)
                }
            }
        }.scan(ExchangeRateState.Loading as ExchangeRateState) { previous, current ->
            when (current) {
                is ExchangeRateState.Loading if previous is ExchangeRateState.Success -> {
                    previous.copy(isRefreshing = true, syncError = null)
                }

                is ExchangeRateState.Error if previous is ExchangeRateState.Success -> {
                    // Non-blocking error: keep showing data but notify user
                    viewModelScope.launch {
                        _uiEvents.emit(ExchangeRateUiEvent.ShowOfflineNotification)
                    }
                    previous.copy(isRefreshing = false, syncError = current.error)
                }

                else -> current
            }
        }
        .distinctUntilChanged()

    @OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
    val state: StateFlow<ExchangeRateUiState> = combine(
        _currentBase,
        _searchQuery.debounce(300.milliseconds).distinctUntilChanged(),
        contentState
    ) { base, query, content ->
        val filteredContent = if (content is ExchangeRateState.Success && query.isNotBlank()) {
            withContext(dispatchers.default) {
                content.copy(
                    pinnedRates = content.pinnedRates.filter {
                        it.code.contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true)
                    }.toImmutableList(),
                    otherRates = content.otherRates.filter {
                        it.code.contains(query, ignoreCase = true) ||
                                it.name.contains(query, ignoreCase = true)
                    }.toImmutableList()
                )
            }
        } else {
            content
        }
        ExchangeRateUiState(base, query, filteredContent)
    }.catch { _ ->
        emit(
            ExchangeRateUiState(
                _currentBase.value,
                _searchQuery.value,
                ExchangeRateState.Error(DataError.Network.UNKNOWN)
            )
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ExchangeRateUiState(_currentBase.value)
    )

    private fun mapToContentState(
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

            currenciesResult is Result.Error -> ExchangeRateState.Error(currenciesResult.error)
            ratesResult is Result.Error -> ExchangeRateState.Error(ratesResult.error)
            else -> ExchangeRateState.Loading
        }
    }

    fun onAction(action: ExchangeRateAction) {
        when (action) {
            is ExchangeRateAction.ChangeBaseCurrency -> {
                _currentBase.value = action.code
            }

            is ExchangeRateAction.SearchQueryChanged -> {
                _searchQuery.value = action.query
            }

            ExchangeRateAction.Refresh -> {
                viewModelScope.launch {
                    _refreshTrigger.emit(Unit)
                }
            }

            is ExchangeRateAction.TogglePin -> {
                viewModelScope.launch {
                    togglePinUseCase(action.code)
                }
            }
        }
    }
}

sealed interface ExchangeRateUiEvent {
    data object ShowOfflineNotification : ExchangeRateUiEvent
}
