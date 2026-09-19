package dev.gustavo.finance.data.util

import dev.gustavo.finance.data.mapper.toDataError
import dev.gustavo.finance.domain.util.DataError
import dev.gustavo.finance.domain.util.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * A reusable synchronization utility to abstract the "Cache-First with Network Refresh" pattern.
 *
 * @param ResultType The type of data returned to the UI.
 * @param RequestType The type of data returned from the network.
 *
 * @param queryOnce To get the initial cached data.
 * @param queryFlow To observe the database for updates.
 * @param fetch To get fresh data from the network.
 * @param saveFetchResult To persist the network data to the database.
 * @param shouldFetch To determine if a network refresh is needed.
 * @param onFetchFailed A callback for logging/tracking when network fails.
 * @param onQueryFailed A callback for logging/tracking when database query/observation fails.
 * @param mapError A function to map [Throwable] to [DataError.Network].
 */
fun <ResultType, RequestType> networkBoundResource(
    queryOnce: suspend () -> ResultType?,
    queryFlow: () -> Flow<ResultType>,
    fetch: suspend () -> RequestType,
    saveFetchResult: suspend (RequestType) -> Unit,
    shouldFetch: suspend (ResultType?) -> Boolean = { true },
    onFetchFailed: (Throwable) -> Unit = { },
    onQueryFailed: (Throwable) -> Unit = { },
    mapError: (Throwable) -> DataError.Network = { it.toDataError() }
): Flow<Result<ResultType, DataError.Network>> = channelFlow {
    var queryOnceError: Throwable? = null
    val cachedData = try {
        queryOnce()
    } catch (e: Throwable) {
        queryOnceError = e
        null
    }
    
    // Always emit Loading with the current cached data first
    send(Result.Loading(cachedData))

    // If initial query failed, emit the error
    queryOnceError?.let {
        onQueryFailed(it)
        send(Result.Error(mapError(it)))
    }

    // 2. Trigger fetch in a background job
    launch {
        try {
            if (shouldFetch(cachedData)) {
                saveFetchResult(fetch())
            }
        } catch (throwable: Throwable) {
            onFetchFailed(throwable)
            send(Result.Error(mapError(throwable)))
        }
    }

    // 3. Continuously collect from queryFlow and emit as Success
    try {
        queryFlow().collect {
            send(Result.Success(it))
        }
    } catch (e: Throwable) {
        onQueryFailed(e)
        send(Result.Error(mapError(e)))
    }
}.distinctUntilChanged()
