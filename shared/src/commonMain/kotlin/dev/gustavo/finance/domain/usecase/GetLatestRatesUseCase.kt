package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result

class GetLatestRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(base: String): Result<ExchangeRatesResponse, DataError.Network> {
        return repository.getLatestRates(base)
    }
}
