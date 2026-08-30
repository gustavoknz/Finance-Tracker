package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrencyDao : CurrencyDao {
    private val currenciesFlow = MutableStateFlow<List<CurrencyEntity>>(emptyList())
    var shouldThrow = false

    override fun getAllCurrencies(): Flow<List<CurrencyEntity>> {
        if (shouldThrow) throw RuntimeException("DB Error")
        return currenciesFlow
    }

    override suspend fun getAllCurrenciesOnce(): List<CurrencyEntity> {
        if (shouldThrow) throw RuntimeException("DB Error")
        return currenciesFlow.value
    }

    override suspend fun insertCurrencies(currencies: List<CurrencyEntity>) {
        if (shouldThrow) throw RuntimeException("DB Error")
        currenciesFlow.value = currencies
    }

    override suspend fun deleteOldCurrencies(timestamp: Long) {
        if (shouldThrow) throw RuntimeException("DB Error")
        currenciesFlow.value = currenciesFlow.value.filter { it.localTimestamp >= timestamp }
    }
}
