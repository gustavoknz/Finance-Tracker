package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.data.mapper.toDataError
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.first

class RealExchangeRateRepository(
    private val currencyService: CurrencyService,
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao
) : ExchangeRateRepository {
    override suspend fun getLatestRates(base: String): Result<ExchangeRatesResponse, DataError.Network> {
        return try {
            val remoteResponse = currencyService.getLatestRates(base)
            val entities = remoteResponse.rates.map { (targetCode, rate) ->
                ExchangeRateEntity(
                    baseCode = base,
                    targetCode = targetCode,
                    rate = rate,
                    date = remoteResponse.date
                )
            }
            exchangeRateDao.insertRates(entities)
            Result.Success(remoteResponse)
        } catch (e: Exception) {
            val localRates = exchangeRateDao.getRatesByBase(base).first()
            if (localRates.isNotEmpty()) {
                Result.Success(
                    ExchangeRatesResponse(
                        amount = 1.0,
                        base = base,
                        date = localRates.first().date,
                        rates = localRates.associate { it.targetCode to it.rate }
                    )
                )
            } else {
                Result.Error(e.toDataError())
            }
        }
    }

    override suspend fun getCurrencies(): Result<Map<String, String>, DataError.Network> {
        return try {
            val count = currencyDao.getCount()
            if (count == 0) {
                val remoteCurrencies = currencyService.getCurrencies()
                val entities = remoteCurrencies.map { (code, name) ->
                    CurrencyEntity(code = code, name = name)
                }
                currencyDao.insertCurrencies(entities)
                Result.Success(remoteCurrencies)
            } else {
                Result.Success(currencyDao.getAllCurrencies().first().associate { it.code to it.name })
            }
        } catch (e: Exception) {
            Result.Error(e.toDataError())
        }
    }
}
