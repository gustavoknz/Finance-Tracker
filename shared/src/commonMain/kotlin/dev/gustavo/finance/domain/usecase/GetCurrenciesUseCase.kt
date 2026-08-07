package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result

class GetCurrenciesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Result<Map<String, String>, DataError.Network> {
        return repository.getCurrencies()
    }
}
