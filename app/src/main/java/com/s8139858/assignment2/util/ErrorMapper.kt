package com.s8139858.assignment2.util

import androidx.annotation.StringRes
import com.s8139858.assignment2.R

/** Maps an [AppError] to the user-facing message shown for it. */
@StringRes
fun AppError.toMessageRes(): Int = when (this) {
    AppError.InvalidCredentials -> R.string.error_invalid_credentials
    AppError.NoNetwork -> R.string.error_no_internet
    AppError.Timeout -> R.string.error_timeout
    is AppError.Server -> R.string.error_server
    AppError.MalformedResponse -> R.string.error_malformed_response
    is AppError.Unknown -> R.string.error_unknown
}
