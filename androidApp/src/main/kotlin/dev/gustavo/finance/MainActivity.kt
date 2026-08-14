package dev.gustavo.finance

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.gustavo.finance.presentation.rates.ExchangeRateScreen
import dev.gustavo.finance.presentation.rates.ExchangeRateState
import dev.gustavo.finance.presentation.rates.ExchangeRateUiModel
import kotlinx.collections.immutable.persistentListOf

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App {
        ExchangeRateScreen().ExchangeRateScreenContent(
            state = ExchangeRateState.Success(
                base = "EUR",
                pinnedRates = persistentListOf(
                    ExchangeRateUiModel("USD", "Dollar", "$", 1.08, "1.08", isPinned = true)
                ),
                otherRates = persistentListOf(
                    ExchangeRateUiModel("GBP", "Pound", "£", 0.85, "0.85")
                ),
                lastUpdated = "2024-05-20"
            ),
            searchQuery = "",
            onSearchQueryChange = {},
            onRefresh = {},
            onRateClick = {},
            onTogglePin = {}
        )
    }
}
