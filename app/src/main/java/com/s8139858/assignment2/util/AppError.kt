package com.s8139858.assignment2.util

/**
 * User-facing error taxonomy for the whole app. ViewModels map these to
 * string resources; repositories map exceptions to these.
 */
sealed class AppError {

    /** 400/401/403/404 from the auth endpoint: wrong username or password. */
    data object InvalidCredentials : AppError()

    /** Connectivity problem (IOException before/during the call). */
    data object NoNetwork : AppError()

    /** Socket timeout — the free-tier server may be cold-starting. */
    data object Timeout : AppError()

    /** Any 5xx response. */
    data class Server(val code: Int) : AppError()

    /** The response body did not match the expected contract. */
    data object MalformedResponse : AppError()

    /** Anything else. */
    data class Unknown(val cause: Throwable? = null) : AppError()
}
