package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GetCurrenciesUseCaseTest {
    private lateinit var repository: FakeExchangeRateRepository
    private lateinit var getCurrenciesUseCase: GetCurrenciesUseCase

    @BeforeTest
    fun setUp() {
        repository = FakeExchangeRateRepository()
        getCurrenciesUseCase = GetCurrenciesUseCase(repository)
    }

    @Test
    fun `invoke should return currencies from repository`() = runTest {
        val expected = mapOf("USD" to "United States Dollar", "EUR" to "Euro")
        repository.currenciesResult = expected

        val result = getCurrenciesUseCase()

        assertTrue(result is Result.Success)
        assertEquals(expected, result.data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        repository.shouldThrow = true

        val result = getCurrenciesUseCase()

        assertTrue(result is Result.Error)
        assertEquals(DataError.Network.UNKNOWN, result.error)
    }
}
