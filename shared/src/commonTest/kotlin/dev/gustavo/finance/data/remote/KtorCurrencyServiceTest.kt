package dev.gustavo.finance.data.remote

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KtorCurrencyServiceTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient
    private lateinit var service: KtorCurrencyService

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `getLatestRates should return parsed response`() = runTest {
        val expectedResponse = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        val responseJson = json.encodeToString(expectedResponse)

        mockEngine = MockEngine { _ ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://api.frankfurter.dev/v1/") }
        }
        service = KtorCurrencyService(httpClient)

        val result = service.getLatestRates("USD")

        assertEquals(expectedResponse, result)
        assertEquals("https://api.frankfurter.dev/v1/latest?base=USD", mockEngine.requestHistory[0].url.toString())
    }

    @Test
    fun `getCurrencies should return parsed map`() = runTest {
        val expectedCurrencies = mapOf("USD" to "United States Dollar", "EUR" to "Euro")
        val responseJson = json.encodeToString(expectedCurrencies)

        mockEngine = MockEngine { _ ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://api.frankfurter.dev/v1/") }
        }
        service = KtorCurrencyService(httpClient)

        val result = service.getCurrencies()

        assertEquals(expectedCurrencies, result)
        assertEquals("https://api.frankfurter.dev/v1/currencies", mockEngine.requestHistory[0].url.toString())
    }

    @Test
    fun `getLatestRates should throw when network fails`() = runTest {
        mockEngine = MockEngine { _ ->
            respond(
                content = "Error",
                status = HttpStatusCode.InternalServerError
            )
        }
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://api.frankfurter.dev/v1/") }
        }
        service = KtorCurrencyService(httpClient)

        assertFailsWith<Exception> {
            service.getLatestRates("USD")
        }
    }

    @Test
    fun `getLatestRates should retry on server error and eventually succeed`() = runTest {
        val expectedResponse = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        val responseJson = json.encodeToString(expectedResponse)
        var callCount = 0
        
        mockEngine = MockEngine { _ ->
            callCount++
            if (callCount < 3) {
                respond(
                    content = "Service Unavailable",
                    status = HttpStatusCode.ServiceUnavailable
                )
            } else {
                respond(
                    content = responseJson,
                    status = HttpStatusCode.OK,
                    headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
                )
            }
        }
        
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
            defaultRequest { url("https://api.frankfurter.dev/v1/") }
            install(HttpRequestRetry) {
                maxRetries = 3
                retryIf { _, response -> !response.status.isSuccess() && response.status.value >= 500 }
                delayMillis { 0 } // No delay in tests
            }
        }
        service = KtorCurrencyService(httpClient)

        val result = service.getLatestRates("USD")

        assertEquals(expectedResponse, result)
        assertEquals(3, callCount)
    }
}
