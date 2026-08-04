package dev.gustavo.finance.domain.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): ExchangeRatesResponse
    suspend fun getCurrencies(): Map<String, String>
}
