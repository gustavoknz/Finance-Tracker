package dev.gustavo.finance.data.mapper

import dev.gustavo.finance.domain.util.DataError
import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.client.plugins.ServerResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import kotlinx.io.IOException
import kotlinx.serialization.SerializationException

private const val HTTP_CLIENT_ERROR_START = 400
private const val HTTP_CLIENT_ERROR_END = 499
private const val HTTP_SERVER_ERROR_START = 500
private const val HTTP_SERVER_ERROR_END = 599

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
                in HTTP_CLIENT_ERROR_START..HTTP_CLIENT_ERROR_END -> DataError.Network.CLIENT_ERROR
                in HTTP_SERVER_ERROR_START..HTTP_SERVER_ERROR_END -> DataError.Network.SERVER_ERROR
                else -> DataError.Network.UNKNOWN
            }
        }

        else -> DataError.Network.UNKNOWN
    }
}
