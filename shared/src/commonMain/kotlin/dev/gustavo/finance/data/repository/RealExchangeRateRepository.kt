package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository

class RealExchangeRateRepository(
    private val currencyService: CurrencyService
) : ExchangeRateRepository {
    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
        return currencyService.getLatestRates(base)
    }
}
