package dev.gustavo.finance.domain.util

sealed interface Result<out D, out E: Error> {
    data class Success<out D>(val data: D): Result<D, Nothing>
    data class Error<out E: dev.gustavo.finance.domain.util.Error>(val error: E): Result<Nothing, E>
}

inline fun <T, E: dev.gustavo.finance.domain.util.Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when(this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
    }
}

fun <T, E: dev.gustavo.finance.domain.util.Error> Result<T, E>.asEmptyData(): Result<Unit, E> {
    return map {  }
}

inline fun <T, E: dev.gustavo.finance.domain.util.Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }
    }
}
inline fun <T, E: dev.gustavo.finance.domain.util.Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when(this) {
        is Result.Error -> {
            action(error)
            this
        }
        is Result.Success -> this
    }
}

typealias DomainResult<D> = Result<D, DataError>
