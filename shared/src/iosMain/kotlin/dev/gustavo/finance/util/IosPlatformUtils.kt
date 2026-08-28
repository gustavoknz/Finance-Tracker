package dev.gustavo.finance.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import platform.Foundation.NSNumberFormatterDecimalStyle

class IosPlatformUtils : PlatformUtils {
    override fun getCurrencySymbol(code: String): String {
        if (code.isEmpty()) return ""
        val formatter = NSNumberFormatter()
        formatter.numberStyle = NSNumberFormatterCurrencyStyle
        formatter.currencyCode = code
        return formatter.currencySymbol
    }

    override fun formatDecimal(value: Double, decimals: Int): String {
        val formatter = NSNumberFormatter()
        formatter.minimumFractionDigits = decimals.toULong()
        formatter.maximumFractionDigits = decimals.toULong()
        formatter.numberStyle = NSNumberFormatterDecimalStyle
        return formatter.stringFromNumber(NSNumber(value)) ?: value.toString()
    }
}
