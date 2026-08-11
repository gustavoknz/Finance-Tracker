package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeExchangeRateDao : ExchangeRateDao {
    private val ratesFlow = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())

    override fun getRatesByBase(baseCode: String): Flow<List<ExchangeRateEntity>> {
        return ratesFlow.map { list -> list.filter { it.baseCode == baseCode } }
    }

    override suspend fun getRatesByBaseOnce(baseCode: String): List<ExchangeRateEntity> {
        return ratesFlow.value.filter { it.baseCode == baseCode }
    }

    override suspend fun insertRates(rates: List<ExchangeRateEntity>) {
        val current = ratesFlow.value.toMutableList()
        // Simple mock of REPLACE strategy
        rates.forEach { newRate ->
            current.removeAll { it.baseCode == newRate.baseCode && it.targetCode == newRate.targetCode }
            current.add(newRate)
        }
        ratesFlow.value = current
    }
}
