package dev.gustavo.finance.util

expect fun getCurrencySymbol(code: String): String

expect fun Double.format(decimals: Int): String
