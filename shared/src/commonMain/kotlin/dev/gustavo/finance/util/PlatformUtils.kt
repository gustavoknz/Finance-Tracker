package dev.gustavo.finance.util

interface PlatformUtils {
    fun getCurrencySymbol(code: String): String
    fun formatDecimal(value: Double, decimals: Int): String
}
