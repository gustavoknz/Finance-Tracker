package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.ExchangeRateRepository

class GetCurrenciesUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(): Map<String, String> {
        return repository.getCurrencies()
    }
}
