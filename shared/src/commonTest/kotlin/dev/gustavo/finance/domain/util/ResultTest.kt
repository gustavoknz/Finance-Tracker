package dev.gustavo.finance.domain.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ResultTest {

    @Test
    fun `map should transform success data`() {
        val result: Result<Int, DataError.Network> = Result.Success(10)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is Result.Success)
        assertEquals(20, mapped.data)
    }

    @Test
    fun `map should propagate error and loading`() {
        val error: Result<Int, DataError.Network> = Result.Error(DataError.Network.UNKNOWN)
        assertEquals(error, error.map { it * 2 })

        val loading: Result<Int, DataError.Network> = Result.Loading()
        assertEquals(Result.Loading<Int>(), loading.map { it * 2 })
    }

    @Test
    fun `flatMap should transform success to new result`() {
        val result: Result<Int, DataError.Network> = Result.Success(10)
        val flatMapped = result.flatMap { Result.Success(it.toString()) }

        assertTrue(flatMapped is Result.Success)
        assertEquals("10", flatMapped.data)
    }

    @Test
    fun `onSuccess should be called only for Success`() {
        var called = false
        Result.Success(1).onSuccess { called = true }
        assertTrue(called)

        called = false
        Result.Error(DataError.Network.UNKNOWN).onSuccess { called = true }
        assertFalse(called)
    }

    @Test
    fun `onError should be called only for Error`() {
        var called = false
        Result.Error(DataError.Network.UNKNOWN).onError { called = true }
        assertTrue(called)

        called = false
        Result.Success(1).onError { called = true }
        assertFalse(called)
    }

    @Test
    fun `getOrNull should return data for Success and Loading with data`() {
        assertEquals(1, Result.Success(1).getOrNull())
        assertNull(Result.Error(DataError.Network.UNKNOWN).getOrNull())
        assertNull(Result.Loading<Int>().getOrNull())
        assertEquals(1, Result.Loading(1).getOrNull())
    }

    @Test
    fun `map should transform loading data`() {
        val result: Result<Int, DataError.Network> = Result.Loading(10)
        val mapped = result.map { it * 2 }

        assertTrue(mapped is Result.Loading)
        assertEquals(20, mapped.data)
    }

    @Test
    fun `asEmptyData should return Unit success`() {
        val result = Result.Success("data").asEmptyData()
        assertTrue(result is Result.Success)
        assertEquals(Unit, result.data)
    }

    @Test
    fun `onSuccess and onError should not be called for Loading`() {
        var successCalled = false
        var errorCalled = false
        val loading = Result.Loading(1)
        loading.onSuccess { successCalled = true }.onError { errorCalled = true }
        assertFalse(successCalled)
        assertFalse(errorCalled)
    }
}
