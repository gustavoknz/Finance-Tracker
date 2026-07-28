package dev.gustavo.finance.data.remote

import dev.gustavo.finance.domain.model.ExchangeRatesResponse

interface CurrencyService {
    suspend fun getLatestRates(base: String): ExchangeRatesResponse
}
