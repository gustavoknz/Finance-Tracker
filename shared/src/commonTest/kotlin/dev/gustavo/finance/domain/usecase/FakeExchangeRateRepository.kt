package dev.gustavo.finance.domain.usecase

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result

class FakeExchangeRateRepository : ExchangeRateRepository {
    var currenciesResult: Map<String, String> = emptyMap()
    var latestRatesResult: ExchangeRatesResponse? = null
    var shouldThrow: Boolean = false

    override suspend fun getLatestRates(base: String): Result<ExchangeRatesResponse, DataError.Network> {
        if (shouldThrow) return Result.Error(DataError.Network.UNKNOWN)
        return latestRatesResult?.let { Result.Success(it) } ?: Result.Error(DataError.Network.UNKNOWN)
    }

    override suspend fun getCurrencies(): Result<Map<String, String>, DataError.Network> {
        if (shouldThrow) return Result.Error(DataError.Network.UNKNOWN)
        return Result.Success(currenciesResult)
    }
}
