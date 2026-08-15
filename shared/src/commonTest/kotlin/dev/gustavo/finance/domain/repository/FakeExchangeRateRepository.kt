package dev.gustavo.finance.domain.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow

class FakeExchangeRateRepository : ExchangeRateRepository {
    var currenciesResult: Map<String, String> = emptyMap()
    var latestRatesResult: ExchangeRatesResponse? = null
    var shouldThrow: Boolean = false
    var emitLoadingOnly: Boolean = false

    private val _pinnedCurrencies = MutableStateFlow<Set<String>>(emptySet())

    override fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>> = flow {
        emit(Result.Loading())
        if (emitLoadingOnly) return@flow
        
        if (shouldThrow) {
            emit(Result.Error(DataError.Network.UNKNOWN))
        } else {
            latestRatesResult?.let { emit(Result.Success(it)) } ?: emit(Result.Error(DataError.Network.UNKNOWN))
        }
    }

    override fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>> = flow {
        emit(Result.Loading())
        if (emitLoadingOnly) return@flow
        
        if (shouldThrow) {
            emit(Result.Error(DataError.Network.UNKNOWN))
        } else {
            emit(Result.Success(currenciesResult))
        }
    }

    override fun getPinnedCurrencies(): Flow<Set<String>> = _pinnedCurrencies.asStateFlow()

    override suspend fun togglePin(code: String) {
        val current = _pinnedCurrencies.value
        if (current.contains(code)) {
            _pinnedCurrencies.value = current - code
        } else {
            _pinnedCurrencies.value = current + code
        }
    }
}
