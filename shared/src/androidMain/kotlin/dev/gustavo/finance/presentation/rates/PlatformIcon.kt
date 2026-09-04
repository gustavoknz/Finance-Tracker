package dev.gustavo.finance.presentation.rates

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Android
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
actual fun PlatformIcon(modifier: Modifier) {
    Icon(
        imageVector = Icons.Default.Android,
        contentDescription = null,
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier
    )
}
