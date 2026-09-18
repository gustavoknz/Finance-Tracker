package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.data.local.ExchangeRateEntity
import dev.gustavo.finance.domain.model.ExchangeRatesResponse

fun List<ExchangeRateEntity>.toResponse(base: String): ExchangeRatesResponse? {
    if (isEmpty()) return null
    
    return ExchangeRatesResponse(
        amount = 1.0,
        base = base,
        date = first().date,
        rates = associate { it.targetCode to it.rate },
    )
}

fun ExchangeRatesResponse.toEntities(timestamp: Long): List<ExchangeRateEntity> {
    return rates.map { (targetCode, rate) ->
        ExchangeRateEntity(
            baseCode = base,
            targetCode = targetCode,
            rate = rate,
            date = date,
            localTimestamp = timestamp,
        )
    }
}
