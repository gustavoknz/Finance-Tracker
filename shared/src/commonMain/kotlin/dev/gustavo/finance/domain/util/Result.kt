package dev.gustavo.finance.domain.util

sealed interface Result<out D, out E : Error> {
    data class Success<out D>(val data: D) : Result<D, Nothing>
    data class Error<out E : dev.gustavo.finance.domain.util.Error>(val error: E) : Result<Nothing, E>
    data class Loading<out D>(val data: D? = null) : Result<D, Nothing>
}

inline fun <T, E : Error, R> Result<T, E>.map(transform: (T) -> R): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> Result.Success(transform(data))
        is Result.Loading -> Result.Loading(data?.let(transform))
    }
}

inline fun <T, E : Error, R> Result<T, E>.flatMap(transform: (T) -> Result<R, E>): Result<R, E> {
    return when (this) {
        is Result.Error -> Result.Error(error)
        is Result.Success -> transform(data)
        is Result.Loading -> {
            data?.let { transform(it) } ?: Result.Loading()
        }
    }
}

fun <T, E : Error> Result<T, E>.asEmptyData(): Result<Unit, E> {
    return map { }
}

inline fun <T, E : Error> Result<T, E>.onSuccess(action: (T) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> this
        is Result.Success -> {
            action(data)
            this
        }

        is Result.Loading -> this
    }
}

inline fun <T, E : Error> Result<T, E>.onError(action: (E) -> Unit): Result<T, E> {
    return when (this) {
        is Result.Error -> {
            action(error)
            this
        }

        is Result.Success -> this
        is Result.Loading -> this
    }
}

fun <T, E : Error> Result<T, E>.getOrNull(): T? {
    return when (this) {
        is Result.Error -> null
        is Result.Success -> data
        is Result.Loading -> data
    }
}
