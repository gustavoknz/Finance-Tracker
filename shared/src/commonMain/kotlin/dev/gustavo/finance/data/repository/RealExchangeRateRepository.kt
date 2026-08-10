package dev.gustavo.finance.data.repository

import dev.gustavo.finance.data.local.CurrencyDao
import dev.gustavo.finance.data.local.CurrencyEntity
import dev.gustavo.finance.data.local.ExchangeRateDao
import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.data.local.MetadataDao
import dev.gustavo.finance.data.local.MetadataEntity
import dev.gustavo.finance.data.mapper.toDataError
import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import dev.gustavo.finance.domain.repository.ExchangeRateRepository
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.time.Clock

class RealExchangeRateRepository(
    private val currencyService: CurrencyService,
    private val currencyDao: CurrencyDao,
    private val exchangeRateDao: ExchangeRateDao,
    private val metadataDao: MetadataDao
) : ExchangeRateRepository {

    companion object {
        private const val CURRENCIES_TTL = 24 * 60 * 60 * 1000L // 24 hours
    }

    override fun getLatestRates(base: String): Flow<Result<ExchangeRatesResponse, DataError.Network>> = channelFlow {
        send(Result.Loading)

        launch {
            try {
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
            } catch (e: Exception) {
                if (exchangeRateDao.getRatesByBase(base).first().isEmpty()) {
                    send(Result.Error(e.toDataError()))
                }
            }
        }

        exchangeRateDao.getRatesByBase(base)
            .map { entities ->
                if (entities.isNotEmpty()) {
                    Result.Success(
                        ExchangeRatesResponse(
                            amount = 1.0,
                            base = base,
                            date = entities.first().date,
                            rates = entities.associate { it.targetCode to it.rate }
                        )
                    )
                } else null
            }
            .filterNotNull()
            .collect { send(it) }
    }.distinctUntilChanged()

    override fun getCurrencies(): Flow<Result<Map<String, String>, DataError.Network>> = channelFlow {
        send(Result.Loading)

        launch {
            try {
                val lastUpdatedMillis = metadataDao.getLastUpdatedTimestamp("currencies")
                val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
                val isStale = if (lastUpdatedMillis == null) {
                    true
                } else {
                    (currentTimeMillis - lastUpdatedMillis) > CURRENCIES_TTL
                }

                if (isStale) {
                    val remoteCurrencies = currencyService.getCurrencies()
                    val entities = remoteCurrencies.map { (code, name) ->
                        CurrencyEntity(code = code, name = name)
                    }
                    currencyDao.insertCurrencies(entities)
                    metadataDao.insertMetadata(MetadataEntity("currencies", currentTimeMillis))
                }
            } catch (e: Exception) {
                if (currencyDao.getAllCurrencies().first().isEmpty()) {
                    send(Result.Error(e.toDataError()))
                }
            }
        }

        currencyDao.getAllCurrencies()
            .map { entities ->
                if (entities.isNotEmpty()) {
                    Result.Success(entities.associate { it.code to it.name })
                } else null
            }
            .filterNotNull()
            .collect { send(it) }
    }.distinctUntilChanged()
}
