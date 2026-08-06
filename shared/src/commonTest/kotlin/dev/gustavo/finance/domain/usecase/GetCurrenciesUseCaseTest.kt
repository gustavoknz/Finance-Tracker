package dev.gustavo.finance.domain.usecase

import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

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

        assertEquals(expected, result)
    }

    @Test
    fun `invoke should throw when repository fails`() = runTest {
        repository.shouldThrow = true

        assertFailsWith<Exception> {
            getCurrenciesUseCase()
        }
    }
}
