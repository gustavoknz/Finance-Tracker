package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse

class FakeCurrencyService : CurrencyService {
    var latestRatesResult: ExchangeRatesResponse? = null
    var currenciesResult: Map<String, String>? = null
    var shouldThrow: Boolean = false

    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
        if (shouldThrow) throw Exception("Network error")
        return latestRatesResult ?: throw Exception("No rates set")
    }

    override suspend fun getCurrencies(): Map<String, String> {
        if (shouldThrow) throw Exception("Network error")
        return currenciesResult ?: throw Exception("No currencies set")
    }
}
