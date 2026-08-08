package dev.gustavo.finance.presentation.rates

data class ExchangeRateUiModel(
    val code: String,
    val name: String,
    val symbol: String,
    val rate: Double,
    val formattedRate: String
)
