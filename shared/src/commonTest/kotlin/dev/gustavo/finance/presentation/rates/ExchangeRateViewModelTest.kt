package dev.gustavo.finance.presentation.rates

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.FakeExchangeRateRepository
import dev.gustavo.finance.domain.repository.FakePreferencesRepository
import dev.gustavo.finance.domain.usecase.GetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.GetCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.GetLatestRatesUseCase
import dev.gustavo.finance.domain.usecase.GetPinnedCurrenciesUseCase
import dev.gustavo.finance.domain.usecase.SetBaseCurrencyUseCase
import dev.gustavo.finance.domain.usecase.TogglePinUseCase
import dev.gustavo.finance.domain.util.DataError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ExchangeRateViewModelTest {

    private lateinit var viewModel: ExchangeRateViewModel
    private lateinit var repository: FakeExchangeRateRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repository = FakeExchangeRateRepository()
        preferencesRepository = FakePreferencesRepository()
        // Use a base different from USD to trigger transitions more clearly
        preferencesRepository.storedBaseCurrency = "BRL"
        
        viewModel = ExchangeRateViewModel(
            getCurrenciesUseCase = GetCurrenciesUseCase(repository),
            getLatestRatesUseCase = GetLatestRatesUseCase(repository),
            setBaseCurrencyUseCase = SetBaseCurrencyUseCase(preferencesRepository),
            getPinnedCurrenciesUseCase = GetPinnedCurrenciesUseCase(repository),
            togglePinUseCase = TogglePinUseCase(repository),
            displayMapper = ExchangeRateDisplayMapper(),
            getBaseCurrencyUseCase = GetBaseCurrencyUseCase(preferencesRepository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertEquals(ExchangeRateState.Loading, viewModel.state.value)
    }

    @Test
    fun `successful data load should emit Success state`() = runTest {
        val currencies = mapOf("USD" to "Dollar", "EUR" to "Euro")
        val rates = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        
        repository.currenciesResult = currencies
        repository.latestRatesResult = rates
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { 
            viewModel.state.collect {} 
        }
        
        viewModel.fetchRates("EUR")
        
        val state = viewModel.state.value
        
        assertTrue(state is ExchangeRateState.Success, "Expected Success state but was $state")
        assertEquals("EUR", state.base)
        assertEquals(1, state.otherRates.size)
        assertEquals("USD", state.otherRates[0].code)
        assertEquals("Dollar", state.otherRates[0].name)
        assertFalse(state.isRefreshing)
    }

    @Test
    fun `error in currencies should emit Error state`() = runTest {
        repository.shouldThrow = true
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { 
            viewModel.state.collect {} 
        }
        
        viewModel.fetchRates("EUR")
        
        val state = viewModel.state.value
        
        assertTrue(state is ExchangeRateState.Error, "Expected Error state but was $state")
        assertEquals(DataError.Network.UNKNOWN, state.error)
        assertEquals("EUR", state.base)
    }

    @Test
    fun `error in rates should emit Error state`() = runTest {
        repository.currenciesResult = mapOf("USD" to "Dollar")
        repository.shouldThrow = true
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { 
            viewModel.state.collect {} 
        }
        
        viewModel.fetchRates("EUR")
        
        val state = viewModel.state.value
        
        assertTrue(state is ExchangeRateState.Error, "Expected Error state but was $state")
        assertEquals("EUR", state.base)
    }

    @Test
    fun `fetchRates should update base and trigger new data load`() = runTest {
        val currencies = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.currenciesResult = currencies
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { 
            viewModel.state.collect {} 
        }
        
        viewModel.fetchRates("USD")
        assertEquals("USD", (viewModel.state.value as ExchangeRateState.Success).base)
        
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        viewModel.fetchRates("EUR")
        
        val newState = viewModel.state.value as ExchangeRateState.Success
        assertEquals("EUR", newState.base)
        assertEquals("EUR", preferencesRepository.storedBaseCurrency)
    }
    
    @Test
    fun `loading state when data is already success should show refreshing`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        // 1. Success
        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        viewModel.fetchRates("USD")
        
        val initialState = viewModel.state.value
        assertTrue(initialState is ExchangeRateState.Success)
        assertFalse(initialState.isRefreshing)

        // 2. Trigger loading for DIFFERENT base to see transition
        repository.emitLoadingOnly = true
        viewModel.fetchRates("EUR")
        
        val refreshingState = viewModel.state.value
        assertTrue(refreshingState is ExchangeRateState.Success, "Expected Success (refreshing) but was $refreshingState")
        assertTrue(refreshingState.isRefreshing)
        assertEquals("USD", refreshingState.base) 
    }

    @Test
    fun `sync error should emit UI event and maintain Success state`() = runTest {
        // 1. Initial success
        repository.currenciesResult = mapOf("USD" to "Dollar")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        
        val events = mutableListOf<ExchangeRateUiEvent>()
        val eventJob = backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiEvents.collect { events.add(it) }
        }
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.fetchRates("EUR")

        // 2. Trigger error while already having data
        repository.shouldThrow = true
        viewModel.refresh()

        val state = viewModel.state.value
        assertTrue(state is ExchangeRateState.Success)
        assertEquals(DataError.Network.UNKNOWN, state.syncError)
        assertEquals(1, events.size)
        assertTrue(events[0] is ExchangeRateUiEvent.ShowOfflineNotification)
        
        eventJob.cancel()
    }

    @Test
    fun `onSearchQueryChange should filter rates`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro", "BRL" to "Real")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08, "BRL" to 5.5))
        viewModel.fetchRates("EUR")
        
        val initialState = viewModel.state.value as ExchangeRateState.Success
        assertEquals(2, initialState.otherRates.size)

        // Filter by code
        viewModel.onSearchQueryChange("US")
        val filteredByCode = viewModel.state.value as ExchangeRateState.Success
        assertEquals(1, filteredByCode.otherRates.size)
        assertEquals("USD", filteredByCode.otherRates[0].code)

        // Filter by name
        viewModel.onSearchQueryChange("Real")
        val filteredByName = viewModel.state.value as ExchangeRateState.Success
        assertEquals(1, filteredByName.otherRates.size)
        assertEquals("BRL", filteredByName.otherRates[0].code)

        // Clear filter
        viewModel.onSearchQueryChange("")
        val clearedFilter = viewModel.state.value as ExchangeRateState.Success
        assertEquals(2, clearedFilter.otherRates.size)
    }

    @Test
    fun `togglePin should update pinned state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        viewModel.fetchRates("EUR")

        val initialState = viewModel.state.value as ExchangeRateState.Success
        assertEquals(1, initialState.otherRates.size)
        assertEquals(0, initialState.pinnedRates.size)

        viewModel.togglePin("USD")

        val pinnedState = viewModel.state.value as ExchangeRateState.Success
        assertEquals(0, pinnedState.otherRates.size)
        assertEquals(1, pinnedState.pinnedRates.size)
        assertEquals("USD", pinnedState.pinnedRates[0].code)
        assertTrue(pinnedState.pinnedRates[0].isPinned)
    }
}
