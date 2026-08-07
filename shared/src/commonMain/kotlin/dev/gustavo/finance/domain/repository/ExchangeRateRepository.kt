package dev.gustavo.finance.domain.repository

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result

interface ExchangeRateRepository {
    suspend fun getLatestRates(base: String): Result<ExchangeRatesResponse, DataError.Network>
    suspend fun getCurrencies(): Result<Map<String, String>, DataError.Network>
}
