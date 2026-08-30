package dev.gustavo.finance.data.fake

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeCurrencyDao : CurrencyDao {
    private val currenciesFlow = MutableStateFlow<List<CurrencyEntity>>(emptyList())

    override fun getAllCurrencies(): Flow<List<CurrencyEntity>> = currenciesFlow

    override suspend fun getAllCurrenciesOnce(): List<CurrencyEntity> = currenciesFlow.value

    override suspend fun insertCurrencies(currencies: List<CurrencyEntity>) {
        currenciesFlow.value = currencies
    }

    override suspend fun deleteOldCurrencies(timestamp: Long) {
        currenciesFlow.value = currenciesFlow.value.filter { it.localTimestamp >= timestamp }
    }
}
