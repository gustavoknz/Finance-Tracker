package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.PreferencesRepository

class SetBaseCurrencyUseCase(
    private val repository: PreferencesRepository
) {
    operator fun invoke(code: String) {
        repository.setBaseCurrency(code)
    }
}
