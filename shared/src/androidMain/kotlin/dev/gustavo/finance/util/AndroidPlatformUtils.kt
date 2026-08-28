package dev.gustavo.finance.util

import java.util.Currency
import java.util.Locale

class AndroidPlatformUtils : PlatformUtils {
    override fun getCurrencySymbol(code: String): String {
        return try {
            Currency.getInstance(code).getSymbol(Locale.getDefault())
        } catch (_: Exception) {
            code
        }
    }

    override fun formatDecimal(value: Double, decimals: Int): String {
        return String.format(Locale.getDefault(), "%.${decimals}f", value)
    }
}
