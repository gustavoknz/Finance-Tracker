package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.data.local.CurrencyEntity

fun List<CurrencyEntity>.toCurrencyMap(): Map<String, String> {
    return associate { it.code to it.name }
}

fun Map<String, String>.toCurrencyEntities(timestamp: Long): List<CurrencyEntity> {
    return map { (code, name) ->
        CurrencyEntity(code = code, name = name, localTimestamp = timestamp)
    }
}
