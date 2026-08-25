package dev.gustavo.finance.di

import dev.gustavo.finance.data.remote.CurrencyService
import dev.gustavo.finance.data.remote.KtorCurrencyService
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.io.IOException
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import co.touchlab.kermit.Logger as KermitLogger

val networkModule = module {
    single {
        HttpClient {
            defaultRequest {
                url("https://api.frankfurter.dev/v1/")
                contentType(ContentType.Application.Json)
            }
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    prettyPrint = true
                    isLenient = true
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 15_000
                socketTimeoutMillis = 15_000
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        KermitLogger.withTag("HttpClient").d { message }
                    }
                }
                level = LogLevel.ALL
            }
            install(HttpRequestRetry) {
                maxRetries = 3
                exponentialDelay()
                retryIf { _, response ->
                    !response.status.isSuccess() && response.status.value >= 500
                }
                retryOnExceptionIf { _, cause ->
                    cause is IOException
                }
            }
        }
    }
    singleOf(::KtorCurrencyService) { bind<CurrencyService>() }
}
