package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository

class GetLatestRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(base: String): ExchangeRatesResponse {
        return repository.getLatestRates(base)
    }
}
