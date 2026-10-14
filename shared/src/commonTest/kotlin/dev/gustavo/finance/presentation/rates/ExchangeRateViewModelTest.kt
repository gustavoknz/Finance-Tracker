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
import dev.gustavo.finance.util.BaseViewModelTest
import dev.gustavo.finance.util.CoroutineDispatchers
import dev.gustavo.finance.util.FakeNetworkMonitor
import dev.gustavo.finance.util.FakePlatformUtils
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

class ExchangeRateViewModelTest : BaseViewModelTest() {

    private lateinit var viewModel: ExchangeRateViewModel
    private lateinit var repository: FakeExchangeRateRepository
    private lateinit var preferencesRepository: FakePreferencesRepository
    private lateinit var networkMonitor: FakeNetworkMonitor

    @BeforeTest
    override fun setUp() {
        super.setUp()
        repository = FakeExchangeRateRepository()
        preferencesRepository = FakePreferencesRepository()
        networkMonitor = FakeNetworkMonitor(initialOnline = true)
        // Use a base different from USD to trigger transitions more clearly
        preferencesRepository.storedBaseCurrency = "BRL"

        viewModel = ExchangeRateViewModel(
            getCurrenciesUseCase = GetCurrenciesUseCase(repository),
            getLatestRatesUseCase = GetLatestRatesUseCase(repository),
            setBaseCurrencyUseCase = SetBaseCurrencyUseCase(preferencesRepository),
            getPinnedCurrenciesUseCase = GetPinnedCurrenciesUseCase(repository),
            togglePinUseCase = TogglePinUseCase(repository),
            displayMapper = ExchangeRateDisplayMapper(FakePlatformUtils()),
            dispatchers = CoroutineDispatchers(testDispatcher, testDispatcher, testDispatcher),
            networkMonitor = networkMonitor,
            getBaseCurrencyUseCase = GetBaseCurrencyUseCase(preferencesRepository),
        )
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

        collectState(viewModel.state)
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        val content = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(content)
        assertEquals(1, content.otherRates.size)
        assertEquals("USD", content.otherRates[0].code)
    }

    @Test
    fun `error loading currencies should emit Error state`() = runTest {
        repository.shouldThrowCurrencies = true

        collectState(viewModel.state)
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        val content = viewModel.state.value.content
        assertIs<ExchangeRateState.Error>(content)
        assertEquals(DataError.Network.UNKNOWN, content.error)
    }

    @Test
    fun `error loading rates should emit Error state`() = runTest {
        repository.currenciesResult = mapOf("USD" to "Dollar")
        repository.shouldThrowRates = true

        collectState(viewModel.state)
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        val content = viewModel.state.value.content
        assertIs<ExchangeRateState.Error>(content)
        assertEquals(DataError.Network.UNKNOWN, content.error)
    }

    @Test
    fun `togglePin should call use case`() = runTest {
        val currencies = mapOf("USD" to "Dollar")
        val rates = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        repository.currenciesResult = currencies
        repository.latestRatesResult = rates

        collectState(viewModel.state)
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        viewModel.onAction(ExchangeRateAction.TogglePin("USD"))
        testScheduler.advanceTimeBy(400)

        assertTrue(repository.getPinnedCurrencies().first().contains("USD"))
    }

    @Test
    fun `non blocking error should keep previous success state and notify user`() = runTest {
        val currencies = mapOf("USD" to "Dollar")
        val rates = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        repository.currenciesResult = currencies
        repository.latestRatesResult = rates

        val events = mutableListOf<ExchangeRateUiEvent>()
        backgroundScope.launch(testDispatcher) {
            viewModel.uiEvents.toList(events)
        }

        collectState(viewModel.state)
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        // Now simulate a network error on refresh
        repository.shouldThrowRates = true
        viewModel.onAction(ExchangeRateAction.Refresh)
        testScheduler.advanceTimeBy(400)

        val content = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(content)
        assertEquals(DataError.Network.UNKNOWN, content.syncError)
        assertEquals(1, events.size)
        assertTrue(events[0] is ExchangeRateUiEvent.ShowOfflineNotification)
    }

    @Test
    fun `onSearchQueryChange should filter rates`() = runTest {
        collectState(viewModel.state)

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro", "BRL" to "Real")
        repository.latestRatesResult =
            ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08, "BRL" to 5.5))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        val content = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(content)
        assertEquals(2, content.otherRates.size)

        // Filter by code
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged("US"))
        testScheduler.advanceTimeBy(400)
        val filteredByCode = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(filteredByCode)
        assertEquals(1, filteredByCode.otherRates.size)
        assertEquals("USD", filteredByCode.otherRates[0].code)

        // Filter by name
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged("Real"))
        testScheduler.advanceTimeBy(400)
        val filteredByName = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(filteredByName)
        assertEquals(1, filteredByName.otherRates.size)
        assertEquals("BRL", filteredByName.otherRates[0].code)

        // Clear filter
        viewModel.onAction(ExchangeRateAction.SearchQueryChanged(""))
        testScheduler.advanceTimeBy(400)
        val clearedFilter = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(clearedFilter)
        assertEquals(2, clearedFilter.otherRates.size)
    }

    @Test
    fun `togglePin should update pinned state`() = runTest {
        collectState(viewModel.state)

        repository.currenciesResult = mapOf("USD" to "Dollar", "EUR" to "Euro")
        repository.latestRatesResult = ExchangeRatesResponse(1.0, "EUR", "2024-05-20", mapOf("USD" to 1.08))
        viewModel.onAction(ExchangeRateAction.ChangeBaseCurrency("EUR"))
        testScheduler.advanceTimeBy(400)

        val initialContent = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(initialContent)
        assertEquals(0, initialContent.pinnedRates.size)

        viewModel.onAction(ExchangeRateAction.TogglePin("USD"))
        testScheduler.advanceTimeBy(400)

        val pinnedContent = viewModel.state.value.content
        assertIs<ExchangeRateState.Success>(pinnedContent)
        assertEquals(1, pinnedContent.pinnedRates.size)
        assertEquals("USD", pinnedContent.pinnedRates[0].code)
    }
}
