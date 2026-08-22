package dev.gustavo.finance.presentation.rates

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.v2.runComposeUiTest
import kotlinx.collections.immutable.persistentListOf
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExchangeRateUiTest {

    @Test
    fun testSuccessState() = runComposeUiTest {
        val rates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08"),
            ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
        )

        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                state = ExchangeRateState.Success(
                    base = "EUR",
                    pinnedRates = persistentListOf(),
                    otherRates = rates,
                    lastUpdated = "2024-05-20"
                ),
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {},
                onRateClick = {},
                onTogglePin = {}
            )
        }

        onNodeWithText("Base: EUR").assertIsDisplayed()
        onNodeWithText("USD").assertIsDisplayed()
        onNodeWithText("GBP").assertIsDisplayed()
    }

    @Test
    fun testErrorState() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                state = ExchangeRateState.Error(
                    error = dev.gustavo.finance.domain.util.DataError.Network.NO_INTERNET,
                    base = "EUR"
                ),
                searchQuery = "",
                onSearchQueryChange = {},
                onRefresh = {},
                onRateClick = {},
                onTogglePin = {}
            )
        }

        onNodeWithText("Check your internet connection and try again.").assertIsDisplayed()
        onNodeWithText("Retry").assertIsDisplayed()
    }
}
