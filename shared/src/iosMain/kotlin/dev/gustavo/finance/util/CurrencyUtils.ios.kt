package dev.gustavo.finance.util

import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterDecimalStyle

actual fun Double.format(decimals: Int): String {
    val formatter = NSNumberFormatter()
    formatter.minimumFractionDigits = decimals.toULong()
    formatter.maximumFractionDigits = decimals.toULong()
    formatter.numberStyle = NSNumberFormatterDecimalStyle
    return formatter.stringFromNumber(NSNumber(this)) ?: this.toString()
}
