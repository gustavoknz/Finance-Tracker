package dev.gustavo.finance.util

import java.util.Currency
import java.util.Locale

actual fun getCurrencySymbol(code: String): String {
    return try {
        Currency.getInstance(code).getSymbol(Locale.getDefault())
    } catch (_: Exception) {
        code
    }
}

actual fun Double.format(decimals: Int): String {
    return String.format(Locale.getDefault(), "%.${decimals}f", this)
}
