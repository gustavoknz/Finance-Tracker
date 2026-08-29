package dev.gustavo.finance.presentation.rates

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
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
    fun testLoadingState() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Loading
                ),
                onAction = {}
            )
        }

        onNodeWithTag("loading_indicator").assertIsDisplayed()
    }

    @Test
    fun testSuccessState() = runComposeUiTest {
        val rates = persistentListOf(
            ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08"),
            ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
        )

        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Success(
                        pinnedRates = persistentListOf(),
                        otherRates = rates,
                        lastUpdated = "2024-05-20"
                    )
                ),
                onAction = {}
            )
        }

        onNodeWithTag("exchange_rate_content").assertIsDisplayed()
        onNodeWithTag("rate_item_USD").assertIsDisplayed()
        onNodeWithTag("rate_item_GBP").assertIsDisplayed()
    }

    @Test
    fun testErrorState() = runComposeUiTest {
        setContent {
            ExchangeRateScreen().ExchangeRateScreenContent(
                uiState = ExchangeRateUiState(
                    base = "EUR",
                    content = ExchangeRateState.Error(
                        error = dev.gustavo.finance.domain.util.DataError.Network.NO_INTERNET
                    )
                ),
                onAction = {}
            )
        }

        onNodeWithTag("error_view").assertIsDisplayed()
    }
}
