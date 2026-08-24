package dev.gustavo.finance.domain.util

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : dev.gustavo.finance.domain.util.Error>(val error: E) : Result<Nothing, E>
    data class Loading<out D>(val data: D? = null) : Result<D, Nothing>
}

fun <T, E : Error> Result<T, E>.getOrNull(): T? {
    return when (this) {
        is Result.Error -> null
        is Result.Success -> data
        is Result.Loading -> data
    }
}
