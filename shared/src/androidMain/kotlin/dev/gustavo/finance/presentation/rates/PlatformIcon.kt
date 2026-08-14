package dev.gustavo.finance.presentation.rates

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp

@Composable
actual fun PlatformIcon(modifier: Modifier) {
    Text(
        text = "🤖",
        fontSize = 20.sp,
        modifier = modifier
    )
}
