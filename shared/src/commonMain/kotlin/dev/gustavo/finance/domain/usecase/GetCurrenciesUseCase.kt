package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.Flow

class GetCurrenciesUseCase(
    private val repository: ExchangeRateRepository
) {
    operator fun invoke(): Flow<Result<Map<String, String>, DataError.Network>> {
        return repository.getCurrencies()
    }
}
