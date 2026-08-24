package dev.gustavo.finance.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ExchangeRatesResponse(
    val amount: Double,
    val base: String,
    val date: String,
    val rates: Map<String, Double>
)
