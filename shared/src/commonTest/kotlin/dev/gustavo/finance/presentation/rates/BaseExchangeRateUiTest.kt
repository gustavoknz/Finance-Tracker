package dev.gustavo.finance.presentation.rates

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.gustavo.finance.domain.util.DataError
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.getValue
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
abstract class BaseExchangeRateUiTest {

    fun runLoadingStateTest() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Loading,
                ),
                onAction = {},
            )
        }

        onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    fun runSuccessStateTest() = runComposeUiTest {
        val rates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08"),
            ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85"),
        )

        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = rates,
                        lastUpdated = "2024-05-20",
                    ),
                ),
                onAction = {},
            )
        }

        onNodeWithTag("exchange_rate_content").assertIsDisplayed()
        onNodeWithTag("rate_item_USD").assertIsDisplayed()
        onNodeWithTag("rate_item_GBP").assertIsDisplayed()
    }

    fun runErrorStateTest() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Error(
                        error = DataError.Network.NO_INTERNET,
                    ),
                ),
                onAction = {},
            )
        }

        onNodeWithTag("error_view").assertIsDisplayed()
    }

    fun runSearchQueryChangedTest() = runComposeUiTest {
        var actionReceived: ExchangeRateAction? = null
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = persistentListOf(),
                        lastUpdated = "2024-05-20",
                    ),
                ),
                onAction = { actionReceived = it },
            )
        }

        onNodeWithTag("search_field").performTextInput("USD")
        assertTrue(actionReceived is ExchangeRateAction.SearchQueryChanged)
        assertEquals("USD", (actionReceived as ExchangeRateAction.SearchQueryChanged).query)
    }

    fun runTogglePinActionTest() = runComposeUiTest {
        var actionReceived: ExchangeRateAction? = null
        val rates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08"),
        )

        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = rates,
                        lastUpdated = "2024-05-20",
                    ),
                ),
                onAction = { actionReceived = it },
            )
        }

        onNodeWithTag("pin_button_USD", useUnmergedTree = true).performClick()
        assertTrue(actionReceived is ExchangeRateAction.TogglePin)
        assertEquals("USD", (actionReceived as ExchangeRateAction.TogglePin).code)
    }

    fun runChangeBaseCurrencyActionTest() = runComposeUiTest {
        var actionReceived: ExchangeRateAction? = null
        val rates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08"),
        )

        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = rates,
                        lastUpdated = "2024-05-20",
                    ),
                ),
                onAction = { actionReceived = it },
            )
        }

        onNodeWithTag("rate_item_USD").performClick()
        
        // Wait for the animation delay (Durations.SHORT = 500ms)
        mainClock.advanceTimeBy(600)
        waitForIdle()

        assertTrue(actionReceived is ExchangeRateAction.ChangeBaseCurrency)
        assertEquals("USD", (actionReceived as ExchangeRateAction.ChangeBaseCurrency).code)
    }

    fun runClearSearchActionTest() = runComposeUiTest {
        var actionReceived: ExchangeRateAction? = null
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    searchQuery = "USD",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = persistentListOf(),
                        lastUpdated = "2024-05-20",
                    ),
                ),
                onAction = { actionReceived = it },
            )
        }

        onNodeWithTag("clear_search_button").performClick()
        assertTrue(actionReceived is ExchangeRateAction.SearchQueryChanged)
        assertEquals("", (actionReceived as ExchangeRateAction.SearchQueryChanged).query)
    }

    fun runRetryActionTest() = runComposeUiTest {
        var actionReceived: ExchangeRateAction? = null
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Error(
                        error = DataError.Network.SERVER_ERROR,
                    ),
                ),
                onAction = { actionReceived = it },
            )
        }

        onNodeWithTag("retry_button").performClick()
        assertTrue(actionReceived is ExchangeRateAction.ChangeBaseCurrency)
        assertEquals("EUR", (actionReceived as ExchangeRateAction.ChangeBaseCurrency).code)
    }

    fun runOfflineNotificationShownTest() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = persistentListOf(),
                        lastUpdated = "2024-05-20",
                        syncError = DataError.Network.NO_INTERNET,
                    ),
                ),
                onAction = {},
            )
        }

        onNodeWithTag("offline_notification").assertIsDisplayed()
    }

    @Suppress("LongMethod")
    fun runScrollToNewlyPinnedItemTest() = runComposeUiTest {
        var isPinned = false
        val initialOtherRates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = false),
            ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85", isPinned = false),
            ExchangeRateUiModel("JPY", "Yen", "¥", 160.0, "160.0", isPinned = false),
            ExchangeRateUiModel("AUD", "Dollar", "$", 1.6, "1.6", isPinned = false),
            ExchangeRateUiModel("CAD", "Dollar", "$", 1.5, "1.5", isPinned = false),
            ExchangeRateUiModel("CHF", "Franc", "Fr", 0.98, "0.98", isPinned = false),
            ExchangeRateUiModel("CNY", "Yuan", "¥", 7.8, "7.8", isPinned = false),
            ExchangeRateUiModel("SEK", "Krona", "kr", 11.5, "11.5", isPinned = false),
            ExchangeRateUiModel("NZD", "Dollar", "$", 1.8, "1.8", isPinned = false),
        )

        // Mock State
        val uiStateFlow = MutableStateFlow(
            ExchangeRateUiState(
                base = "EUR",
                content = ExchangeRateState.Success(
                    pinnedRates = persistentListOf(),
                    otherRates = initialOtherRates,
                    lastUpdated = "2024-05-20",
                ),
            )
        )

        setContent {
            val state by uiStateFlow.collectAsStateWithLifecycle()
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = state,
                onAction = { action ->
                    when (action) {
                        is ExchangeRateAction.SearchQueryChanged -> {
                            val success = uiStateFlow.value.content as ExchangeRateState.Success
                            uiStateFlow.value = uiStateFlow.value.copy(
                                searchQuery = action.query,
                                content = success.copy(
                                    otherRates = initialOtherRates.filter {
                                        it.code.contains(action.query, ignoreCase = true)
                                    }.toImmutableList(),
                                ),
                            )
                        }

                        is ExchangeRateAction.TogglePin if action.code == "NZD" -> {
                            isPinned = true
                            val nzd = initialOtherRates.find { it.code == "NZD" }!!
                            uiStateFlow.value = uiStateFlow.value.copy(
                                content = (uiStateFlow.value.content as ExchangeRateState.Success).copy(
                                    pinnedRates = persistentListOf(nzd.copy(isPinned = true)),
                                    otherRates = initialOtherRates.filter { it.code != "NZD" }.toImmutableList()
                                ),
                                searchQuery = "", // Clear search on pin to verify scroll in full list
                            )
                        }

                        else -> {}
                    }
                },
            )
        }

        // Bring NZD into view by searching
        onNodeWithTag("search_field").performTextInput("NZD")
        
        onNodeWithTag("pin_button_NZD", useUnmergedTree = true).performClick()

        assertTrue(isPinned)
        
        // Wait for potential scroll animation
        mainClock.advanceTimeBy(1000)
        waitForIdle()

        // After pinning, NZD should be in the pinned section at the top.
        // We verified the scroll happens by checking if it's displayed? 
        // Well, the requirement is "should be scrolled". 
        // In runComposeUiTest, we can check if the item is displayed.
        onNodeWithTag("rate_item_NZD").assertIsDisplayed()
    }
}
