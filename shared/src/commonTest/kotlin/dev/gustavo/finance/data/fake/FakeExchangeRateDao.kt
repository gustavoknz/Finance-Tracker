package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeExchangeRateDao : ExchangeRateDao {
    private val ratesFlow = MutableStateFlow<List<ExchangeRateEntity>>(emptyList())
    var shouldThrow = false

    override fun getRatesByBase(baseCode: String): Flow<List<ExchangeRateEntity>> {
        if (shouldThrow) throw RuntimeException("DB Error")
        return ratesFlow.map { list -> list.filter { it.baseCode == baseCode } }
    }

    override suspend fun getRatesByBaseOnce(baseCode: String): List<ExchangeRateEntity> {
        if (shouldThrow) throw RuntimeException("DB Error")
        return ratesFlow.value.filter { it.baseCode == baseCode }
    }

    override suspend fun insertRates(rates: List<ExchangeRateEntity>) {
        if (shouldThrow) throw RuntimeException("DB Error")
        val current = ratesFlow.value.toMutableList()
        // Simple mock of REPLACE strategy
        rates.forEach { newRate ->
            current.removeAll { it.baseCode == newRate.baseCode && it.targetCode == newRate.targetCode }
            current.add(newRate)
        }
        ratesFlow.value = current
    }

    override suspend fun deleteOldRates(timestamp: Long) {
        if (shouldThrow) throw RuntimeException("DB Error")
        ratesFlow.value = ratesFlow.value.filter { it.localTimestamp >= timestamp }
    }
}
