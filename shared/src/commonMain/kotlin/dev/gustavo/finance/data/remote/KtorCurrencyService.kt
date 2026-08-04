package dev.gustavo.finance.data.remote

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorCurrencyService(
    private val httpClient: HttpClient
) : CurrencyService {
    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
        return httpClient.get("https://api.frankfurter.dev/v1/latest") {
            parameter("base", base)
        }.body()
    }

    override suspend fun getCurrencies(): Map<String, String> {
        return httpClient.get("https://api.frankfurter.dev/v1/currencies").body()
    }
}
