package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository

class FakeExchangeRateRepository : ExchangeRateRepository {
    var currenciesResult: Map<String, String> = emptyMap()
    var latestRatesResult: ExchangeRatesResponse? = null
    var shouldThrow: Boolean = false

    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
        if (shouldThrow) throw Exception("Test exception")
        return latestRatesResult ?: throw Exception("Result not set")
    }

    override suspend fun getCurrencies(): Map<String, String> {
        if (shouldThrow) throw Exception("Test exception")
        return currenciesResult
    }
}
