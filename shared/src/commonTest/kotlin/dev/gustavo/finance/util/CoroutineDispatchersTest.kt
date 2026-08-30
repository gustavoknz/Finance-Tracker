package dev.gustavo.finance.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlin.test.Test
import kotlin.test.assertEquals

class CoroutineDispatchersTest {

    @Test
    fun `CoroutineDispatchers should have default values`() {
        val dispatchers = CoroutineDispatchers()
        
        assertEquals(Dispatchers.Main, dispatchers.main)
        assertEquals(Dispatchers.Default, dispatchers.default)
        assertEquals(Dispatchers.IO, dispatchers.io)
    }

    @Test
    fun `CoroutineDispatchers should allow custom values`() {
        val dispatchers = CoroutineDispatchers(
            main = Dispatchers.Unconfined,
            default = Dispatchers.Unconfined,
            io = Dispatchers.Unconfined
        )
        
        assertEquals(Dispatchers.Unconfined, dispatchers.main)
        assertEquals(Dispatchers.Unconfined, dispatchers.default)
        assertEquals(Dispatchers.Unconfined, dispatchers.io)
    }
}
