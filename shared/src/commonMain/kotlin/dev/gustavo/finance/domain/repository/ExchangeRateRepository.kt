package dev.gustavo.finance.domain.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): ExchangeRatesResponse
}
