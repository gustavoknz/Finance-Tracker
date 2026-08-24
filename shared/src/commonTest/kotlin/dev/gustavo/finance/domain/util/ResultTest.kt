package dev.gustavo.finance.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ResultTest {

    @Test
    fun `getOrNull should return data for Success and Loading with data`() {
        assertEquals(1, Result.Success(1).getOrNull())
        assertNull(Result.Error(DataError.Network.UNKNOWN).getOrNull())
        assertNull(Result.Loading<Int>().getOrNull())
        assertEquals(1, Result.Loading(1).getOrNull())
    }
}
