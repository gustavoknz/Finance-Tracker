package dev.gustavo.finance.util

import io.ktor.client.plugins.*
import io.ktor.utils.io.errors.*

sealed interface DataError {
    enum class Network : DataError {
        SERVICE_UNAVAILABLE,
        CLIENT_ERROR,
        SERVER_ERROR,
        UNKNOWN,
        NO_INTERNET
    }
}

fun Exception.toDataError(): DataError.Network {
    return when (this) {
        is IOException -> DataError.Network.NO_INTERNET
        is ResponseException -> {
            when (response.status.value) {
                in 400..499 -> DataError.Network.CLIENT_ERROR
                in 500..599 -> DataError.Network.SERVER_ERROR
                else -> DataError.Network.UNKNOWN
            }
        }
        else -> DataError.Network.UNKNOWN
    }
}
