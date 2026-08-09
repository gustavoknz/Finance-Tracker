package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.PreferencesRepository

class GetBaseCurrencyUseCase(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): String {
        return repository.getBaseCurrency()
    }
}
