package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetLatestRatesUseCaseTest {
    private lateinit var repository: FakeExchangeRateRepository
    private lateinit var getLatestRatesUseCase: GetLatestRatesUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeExchangeRateRepository()
        getLatestRatesUseCase = GetLatestRatesUseCase(repository)
    }

    @Test
    fun `invoke should return latest rates from repository`() = runTest {
        val expected = ExchangeRatesResponse(
            amount = 1.0,
            base = "EUR",
            date = "2024-05-20",
            rates = mapOf("USD" to 1.08)
        )
        repository.latestRatesResult = expected

        val result = getLatestRatesUseCase("EUR")

        assertTrue(result is Result.Success)
        assertEquals(expected, result.data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        repository.shouldThrow = true

        val result = getLatestRatesUseCase("EUR")

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.UNKNOWN, result.error)
    }
}
