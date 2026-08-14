package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.Flow

class GetPinnedCurrenciesUseCase(
    private val repository: ExchangeRateRepository
) {
    operator fun invoke(): Flow<Set<String>> = repository.getPinnedCurrencies()
}
