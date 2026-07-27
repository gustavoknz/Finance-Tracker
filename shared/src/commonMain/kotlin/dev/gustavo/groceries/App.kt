package dev.gustavo.groceries

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import cafe.adriel.voyager.navigator.Navigator
import cafe.adriel.voyager.transitions.SlideTransition
import dev.gustavo.groceries.presentation.products.ProductListScreen

@Composable
@Preview
fun App() {
    MaterialTheme {
        Navigator(ProductListScreen()) { navigator ->
            SlideTransition(navigator)
        }
    }
}
