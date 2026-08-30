package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.FakeExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.toList
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

        val results = getLatestRatesUseCase("EUR").toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(expected, (results[1] as Result.Success).data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        repository.shouldThrowRates = true

        val results = getLatestRatesUseCase("EUR").toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Error)
        assertEquals(DataError.Network.UNKNOWN, (results[1] as Result.Error).error)
    }
}
