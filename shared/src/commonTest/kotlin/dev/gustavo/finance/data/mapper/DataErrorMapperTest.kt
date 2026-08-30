package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.domain.util.DataError
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.client.request.get
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.runTest
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DataErrorMapperTest {

    @Test
    fun `IOException should map to NO_INTERNET`() {
        val exception = IOException("No internet")
        assertEquals(DataError.Network.NO_INTERNET, exception.toDataError())
    }

    @Test
    fun `SerializationException should map to SERVER_ERROR`() {
        val exception = SerializationException("JSON error")
        assertEquals(DataError.Network.SERVER_ERROR, exception.toDataError())
    }

    @Test
    fun `CancellationException should be rethrown`() {
        val exception = CancellationException("Cancelled")
        assertFailsWith<CancellationException> {
            exception.toDataError()
        }
    }

    @Test
    fun `ClientRequestException should map to CLIENT_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.BadRequest)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ClientRequestException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.CLIENT_ERROR, exception.toDataError())
    }

    @Test
    fun `ServerResponseException should map to SERVER_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.InternalServerError)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ServerResponseException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.SERVER_ERROR, exception.toDataError())
    }

    @Test
    fun `ServiceUnavailable should map to SERVICE_UNAVAILABLE`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.ServiceUnavailable)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ResponseException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.SERVICE_UNAVAILABLE, exception.toDataError())
    }

    @Test
    fun `RedirectResponseException should map to UNKNOWN`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.MultipleChoices)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<RedirectResponseException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.UNKNOWN, exception.toDataError())
    }

    @Test
    fun `Forbidden status should map to CLIENT_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.Forbidden)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ClientRequestException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.CLIENT_ERROR, exception.toDataError())
    }

    @Test
    fun `BadGateway status should map to SERVER_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode.BadGateway)
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ServerResponseException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.SERVER_ERROR, exception.toDataError())
    }

    @Test
    fun `Boundary 499 status should map to CLIENT_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode(499, "Unknown Client Error"))
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ClientRequestException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.CLIENT_ERROR, exception.toDataError())
    }

    @Test
    fun `Boundary 599 status should map to SERVER_ERROR`() = runTest {
        val client = HttpClient(MockEngine {
            respond("", HttpStatusCode(599, "Unknown Server Error"))
        }) {
            expectSuccess = true
        }
        val exception = assertFailsWith<ServerResponseException> {
            client.get("https://test.com")
        }
        assertEquals(DataError.Network.SERVER_ERROR, exception.toDataError())
    }

    @Test
    fun `Unknown exception should map to UNKNOWN`() {
        val exception = Exception("Random error")
        assertEquals(DataError.Network.UNKNOWN, exception.toDataError())
    }
}
