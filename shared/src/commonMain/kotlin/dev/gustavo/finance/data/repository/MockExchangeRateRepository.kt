package dev.gustavo.finance.data.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.delay

class MockExchangeRateRepository : ExchangeRateRepository {
    override suspend fun getLatestRates(base: String): Result<ExchangeRatesResponse, DataError.Network> {
        delay(500)
        return Result.Success(
            ExchangeRatesResponse(
                amount = 1.0,
                base = base,
                date = "2024-05-20",
                rates = mapOf(
                    "USD" to 1.0854,
                    "GBP" to 0.8544,
                    "BRL" to 5.5458,
                    "AUD" to 1.6318
                )
            )
        )
    }

    override suspend fun getCurrencies(): Result<Map<String, String>, DataError.Network> {
        delay(300)
        return Result.Success(
            mapOf(
                "USD" to "United States Dollar",
                "GBP" to "British Pound Sterling",
                "BRL" to "Brazilian Real",
                "AUD" to "Australian Dollar",
                "EUR" to "Euro"
            )
        )
    }
}
