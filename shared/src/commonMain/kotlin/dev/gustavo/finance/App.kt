package dev.gustavo.finance

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.gustavo.finance.presentation.expenses.ExpenseListScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(ExpenseListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
