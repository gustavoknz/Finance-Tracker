package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.domain.util.DataError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

fun Throwable.toDataError(): DataError.Network {
    if (this is CancellationException) throw this

    return when (this) {
        is IOException -> DataError.Network.NO_INTERNET
        is SerializationException -> DataError.Network.SERVER_ERROR
        is ClientRequestException -> DataError.Network.CLIENT_ERROR
        is ServerResponseException -> DataError.Network.SERVER_ERROR
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
