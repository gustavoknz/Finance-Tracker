package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import kotlinx.coroutines.flow.first

class RealExchangeRateRepository(
    private val currencyService: CurrencyService,
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao
) : ExchangeRateRepository {
    override suspend fun getLatestRates(base: String): ExchangeRatesResponse {
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
            remoteResponse
        } catch (e: Exception) {
            val localRates = exchangeRateDao.getRatesByBase(base).first()
            if (localRates.isNotEmpty()) {
                ExchangeRatesResponse(
                    amount = 1.0,
                    base = base,
                    date = localRates.first().date,
                    rates = localRates.associate { it.targetCode to it.rate }
                )
            } else {
                throw e
            }
        }
    }

    override suspend fun getCurrencies(): Map<String, String> {
        val count = currencyDao.getCount()
        if (count == 0) {
            val remoteCurrencies = currencyService.getCurrencies()
            val entities = remoteCurrencies.map { (code, name) ->
                CurrencyEntity(code = code, name = name)
            }
            currencyDao.insertCurrencies(entities)
            return remoteCurrencies
        }
        return currencyDao.getAllCurrencies().first().associate { it.code to it.name }
    }
}
