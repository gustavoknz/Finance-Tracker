package dev.gustavo.finance.data.remote

import dev.gustavo.finance.domain.model.ExchangeRatesResponse
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class KtorCurrencyServiceTest {

    private lateinit var mockEngine: MockEngine
    private lateinit var httpClient: HttpClient
    private lateinit var service: KtorCurrencyService

    private val json = Json { ignoreUnknownKeys = true }

    @BeforeTest
    fun setUp() {
        // We'll configure the engine per test to respond differently
    }

    @Test
    fun `getLatestRates should return parsed response`() = runTest {
        val expectedResponse = ExchangeRatesResponse(1.0, "USD", "2024-05-20", mapOf("EUR" to 0.92))
        val responseJson = json.encodeToString(expectedResponse)
        
        mockEngine = MockEngine { request ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
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
        
        mockEngine = MockEngine { request ->
            respond(
                content = responseJson,
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, ContentType.Application.Json.toString())
            )
        }
        httpClient = HttpClient(mockEngine) {
            install(ContentNegotiation) { json(json) }
        }
        service = KtorCurrencyService(httpClient)

        val result = service.getCurrencies()

        assertEquals(expectedCurrencies, result)
        assertEquals("https://api.frankfurter.dev/v1/currencies", mockEngine.requestHistory[0].url.toString())
    }
}
