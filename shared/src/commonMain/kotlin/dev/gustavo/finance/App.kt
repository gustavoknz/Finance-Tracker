package dev.gustavo.finance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.gustavo.finance.presentation.rates.ExchangeRateScreen
import dev.gustavo.finance.presentation.rates.ExchangeRateState
import dev.gustavo.finance.presentation.rates.ExchangeRateUiModel
import dev.gustavo.finance.presentation.rates.ExchangeRateUiState
import kotlinx.collections.immutable.persistentListOf

@Composable
fun App(
    content: @Composable () -> Unit = { ExchangeRateScreen().Content() }
) {
    MaterialTheme {
        content()
    }
}

@Suppress("MagicNumber")
@Composable
@Preview
fun AppPreview() {
    App {
        ExchangeRateScreen().ExchangeRateScreenContent(
            uiState = ExchangeRateUiState(
                base = "EUR",
                searchQuery = "",
                content = ExchangeRateState.Success(
                    pinnedRates = persistentListOf(
                        ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = true)
                    ),
                    otherRates = persistentListOf(
                        ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
                    ),
                    lastUpdated = "2024-05-20"
                )
            ),
            onAction = {}
        )
    }
}
