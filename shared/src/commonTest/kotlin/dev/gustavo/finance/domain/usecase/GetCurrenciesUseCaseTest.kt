package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.repository.FakeExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.toList
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

        val results = getCurrenciesUseCase().toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Success)
        assertEquals(expected, (results[1] as Result.Success).data)
    }

    @Test
    fun `invoke should return error when repository fails`() = runTest {
        repository.shouldThrowCurrencies = true

        val results = getCurrenciesUseCase().toList()

        assertTrue(results[0] is Result.Loading)
        assertTrue(results[1] is Result.Error)
        assertEquals(DataError.Network.UNKNOWN, (results[1] as Result.Error).error)
    }
}
