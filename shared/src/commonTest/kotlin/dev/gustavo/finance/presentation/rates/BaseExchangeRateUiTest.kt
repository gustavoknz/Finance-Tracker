package dev.gustavo.finance.presentation.rates

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.v2.runComposeUiTest
import dev.gustavo.finance.domain.util.DataError
import kotlinx.collections.immutable.persistentListOf
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
}
