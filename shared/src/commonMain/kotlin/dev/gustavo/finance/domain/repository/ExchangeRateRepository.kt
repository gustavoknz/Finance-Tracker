package dev.gustavo.finance.domain.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.Flow

interface ExchangeRateRepository {
    fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>>
    fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>>
}
