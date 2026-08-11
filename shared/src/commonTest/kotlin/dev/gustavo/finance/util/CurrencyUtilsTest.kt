package dev.gustavo.finance.util

import kotlin.test.Test
import kotlin.test.assertEquals

class CurrencyUtilsTest {

    @Test
    fun `getCurrencySymbol should return correct symbol for known codes`() {
        assertEquals("$", getCurrencySymbol("USD"))
        assertEquals("€", getCurrencySymbol("EUR"))
        assertEquals("R$", getCurrencySymbol("BRL"))
        assertEquals("£", getCurrencySymbol("GBP"))
        assertEquals("¥", getCurrencySymbol("JPY"))
    }

    @Test
    fun `getCurrencySymbol should return code for unknown codes`() {
        assertEquals("XYZ", getCurrencySymbol("XYZ"))
        assertEquals("", getCurrencySymbol(""))
    }
}
