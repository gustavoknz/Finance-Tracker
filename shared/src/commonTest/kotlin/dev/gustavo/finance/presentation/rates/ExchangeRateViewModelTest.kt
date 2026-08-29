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
import dev.gustavo.finance.util.FakePlatformUtils
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
            displayMapper = ExchangeRateDisplayMapper(FakePlatformUtils()),
            getBaseCurrencyUseCase = GetBaseCurrencyUseCase(preferencesRepository)
        )
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state should be Loading`() = runTest {
        assertEquals(ExchangeRateState.Loading, viewModel.state.value.content)
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

        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val uiState = viewModel.state.value
        val content = uiState.content

        assertTrue(content is ExchangeRateState.Success, "Expected Success state but was $content")
        assertEquals("EUR", uiState.base)
        assertEquals(1, content.otherRates.size)
        assertEquals("USD", content.otherRates[0].code)
        assertEquals("Dollar", content.otherRates[0].name)
        assertFalse(content.isRefreshing)
    }

    @Test
    fun `error in currencies should emit Error state`() = runTest {
        repository.shouldThrow = true

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val uiState = viewModel.state.value
        val content = uiState.content

        assertTrue(content is ExchangeRateState.Error, "Expected Error state but was $content")
        assertEquals(DataError.Network.UNKNOWN, content.error)
        assertEquals("EUR", uiState.base)
    }

    @Test
    fun `error in rates should emit Error state`() = runTest {
        repository.currenciesResult = mapOf("USD" to "Dollar")
        repository.shouldThrow = true

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val uiState = viewModel.state.value
        val content = uiState.content

        assertTrue(content is ExchangeRateState.Error, "Expected Error state but was $content")
        assertEquals("EUR", uiState.base)
    }

    @Test
    fun `fetchRates should update base and trigger new data load`() = runTest {
        val currencies = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.currenciesResult = currencies
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))

        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.state.collect {}
        }

        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("USD"))
        assertEquals("USD", viewModel.state.value.base)
        assertTrue(viewModel.state.value.content is ExchangeRateState.Success)

        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val newUiState = viewModel.state.value
        assertEquals("EUR", newUiState.base)
        assertEquals("EUR", preferencesRepository.storedBaseCurrency)
    }

    @Test
    fun `loading state when data is already success should show refreshing`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        // 1. Success
        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("USD"))

        val initialUiState = viewModel.state.value
        val initialContent = initialUiState.content
        assertTrue(initialContent is ExchangeRateState.Success)
        assertFalse(initialContent.isRefreshing)

        // 2. Trigger loading for DIFFERENT base to see transition
        repository.emitLoadingOnly = true
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val refreshingUiState = viewModel.state.value
        val refreshingContent = refreshingUiState.content
        assertTrue(
            refreshingContent is ExchangeRateState.Success,
            "Expected Success (refreshing) but was $refreshingContent"
        )
        assertTrue(refreshingContent.isRefreshing)
        assertEquals("USD", initialUiState.base)
        assertEquals("EUR", refreshingUiState.base)
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

        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        // 2. Trigger error while already having data
        repository.shouldThrow = true
        viewModel.onAction(ExchangeRateAction.Refresh)

        val uiState = viewModel.state.value
        val content = uiState.content
        assertTrue(content is ExchangeRateState.Success)
        assertEquals(DataError.Network.UNKNOWN, content.syncError)
        assertEquals(1, events.size)
        assertTrue(events[0] is ExchangeRateUiEvent.ShowOfflineNotification)

        eventJob.cancel()
    }

    @Test
    fun `onSearchQueryChange should filter rates`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro", "BRL" to "Real")
        repository.latestRatesResult =
            ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08, "BRL" to 5.5))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val initialContent = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(2, initialContent.otherRates.size)

        // Filter by code
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged("US"))
        val filteredByCode = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(1, filteredByCode.otherRates.size)
        assertEquals("USD", filteredByCode.otherRates[0].code)

        // Filter by name
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged("Real"))
        val filteredByName = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(1, filteredByName.otherRates.size)
        assertEquals("BRL", filteredByName.otherRates[0].code)

        // Clear filter
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged(""))
        val clearedFilter = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(2, clearedFilter.otherRates.size)
    }

    @Test
    fun `togglePin should update pinned state`() = runTest {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.state.collect {} }

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))

        val initialContent = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(1, initialContent.otherRates.size)
        assertEquals(0, initialContent.pinnedRates.size)

        viewModel.onAction(ExchangeRateAction.TogglePin("USD"))

        val pinnedContent = viewModel.state.value.content as ExchangeRateState.Success
        assertEquals(0, pinnedContent.otherRates.size)
        assertEquals(1, pinnedContent.pinnedRates.size)
        assertEquals("USD", pinnedContent.pinnedRates[0].code)
        assertTrue(pinnedContent.pinnedRates[0].isPinned)
    }
}
