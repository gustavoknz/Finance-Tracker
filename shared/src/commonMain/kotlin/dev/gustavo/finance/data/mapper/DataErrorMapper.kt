package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.domain.util.DataError
import io.ktor.client.plugins.*
import io.ktor.http.HttpStatusCode
import kotlinx.io.IOException

fun Throwable.toDataError(): DataError.Network {
    return when (this) {
        is IOException -> DataError.Network.NO_INTERNET
        is ResponseException -> {
            when (response.status.value) {
                HttpStatusCode.ServiceUnavailable.value -> DataError.Network.SERVICE_UNAVAILABLE
                in 400..499 -> DataError.Network.CLIENT_ERROR
                in 500..599 -> DataError.Network.SERVER_ERROR
                else -> DataError.Network.UNKNOWN
            }
        }
        else -> DataError.Network.UNKNOWN
    }
}
