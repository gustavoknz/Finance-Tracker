package dev.gustavo.finance.data.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.delay

class MockExchangeRateRepository : ExchangeRateRepository {
    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
        delay(500)
        return ExchangeRatesResponse(
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
    }
}
