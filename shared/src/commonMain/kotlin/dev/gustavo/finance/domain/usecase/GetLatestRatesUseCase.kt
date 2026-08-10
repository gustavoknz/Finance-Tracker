package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.Flow

class GetLatestRatesUseCase(
    private val repository: ExchangeRateRepository
) {
    operator fun invoke(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>> {
        return repository.getLatestRates(base)
    }
}
