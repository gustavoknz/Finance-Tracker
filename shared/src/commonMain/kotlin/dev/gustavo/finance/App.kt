package dev.gustavo.finance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.gustavo.finance.di.databaseModule
import dev.gustavo.finance.di.domainModule
import dev.gustavo.finance.di.networkModule
import dev.gustavo.finance.di.viewModelModule
import dev.gustavo.finance.presentation.rates.ExchangeRateScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    MaterialTheme {
        ExchangeRateScreen().Content()
    }
}

@Composable
@Preview
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration {
        modules(
            networkModule,
            databaseModule,
            domainModule,
            viewModelModule
        )
    }) {
        App()
    }
}
