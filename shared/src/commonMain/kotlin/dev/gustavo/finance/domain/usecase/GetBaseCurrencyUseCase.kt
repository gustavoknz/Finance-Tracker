package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetBaseCurrencyUseCase(
    private val repository: PreferencesRepository
) {
    operator fun invoke(): Flow<String> {
        return repository.getBaseCurrencyFlow()
    }
}
