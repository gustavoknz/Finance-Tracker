package dev.gustavo.finance.presentation.rates

import androidx.compose.runtime.Immutable

@Immutable
data class ExchangeRateUiModel(
    val code: String,
    val name: String,
    val symbol: String,
    val rate: Double,
    val formattedRate: String,
    val isPinned: Boolean = false
)
