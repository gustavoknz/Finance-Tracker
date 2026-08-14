package dev.gustavo.finance.domain.util

sealed interface DataError : Error {
    enum class Network : DataError {
        SERVICE_UNAVAILABLE,
        CLIENT_ERROR,
        SERVER_ERROR,
        UNKNOWN,
        NO_INTERNET
    }
}
