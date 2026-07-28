package dev.gustavo.groceries.domain.repository

import dev.gustavo.groceries.domain.model.ExchangeRatesResponse

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): ExchangeRatesResponse
}
