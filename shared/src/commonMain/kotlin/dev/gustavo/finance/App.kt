package dev.gustavo.finance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.gustavo.finance.di.commonModule
import dev.gustavo.finance.presentation.expenses.ExpenseListScreen
import org.koin.compose.KoinApplication
import org.koin.dsl.koinConfiguration

@Composable
fun App() {
    MaterialTheme {
        Navigator(ExpenseListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}

@Composable
@Preview
fun AppPreview() {
    KoinApplication(configuration = koinConfiguration {
        modules(commonModule)
    }) {
        App()
    }
}
