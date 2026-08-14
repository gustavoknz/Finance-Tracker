package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.ExchangeRateRepository

class TogglePinUseCase(
    private val repository: ExchangeRateRepository
) {
    suspend operator fun invoke(code: String) = repository.togglePin(code)
}
