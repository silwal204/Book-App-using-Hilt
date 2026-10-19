package com.s8139858.assignment2.util

import java.io.IOException
import java.net.SocketTimeoutException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import retrofit2.HttpException

/** Thrown when a response is syntactically fine but misses fields the app relies on. */
class MalformedResponseException : Exception("Response body was missing expected fields")

/**
 * Runs [block] on [dispatcher] and wraps the outcome in an [ApiResult],
 * translating every exception into an [AppError].
 *
 * withContext(dispatcher) moves the network work off the main thread onto the
 * injected dispatcher (Dispatchers.IO in production, a TestDispatcher in tests).
 * CancellationException is rethrown on purpose: swallowing it would break
 * structured concurrency (e.g. viewModelScope cancellation when the user leaves).
 */
suspend fun <T> safeApiCall(
    dispatcher: CoroutineDispatcher,
    block: suspend () -> T,
): ApiResult<T> = withContext(dispatcher) {
    try {
        ApiResult.Success(block())
    } catch (cancellation: kotlinx.coroutines.CancellationException) {
        throw cancellation
    } catch (throwable: Throwable) {
        ApiResult.Failure(throwable.toAppError())
    }
}

/** Translates a networking/HTTP exception into the app error taxonomy. */
fun Throwable.toAppError(): AppError = when (this) {
    is MalformedResponseException -> AppError.MalformedResponse
    is HttpException -> when (code()) {
        // The API answers invalid credentials with 400 (wrong password),
        // 404 (unknown user) and may use 401/403 — verified against the live server.
        400, 401, 403, 404 -> AppError.InvalidCredentials
        in 500..599 -> AppError.Server(code())
        else -> AppError.Unknown(this)
    }
    is SocketTimeoutException -> AppError.Timeout
    is IOException -> AppError.NoNetwork
    else -> AppError.Unknown(this)
}
