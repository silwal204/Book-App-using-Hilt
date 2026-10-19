package com.s8139858.assignment2.util

/** Result wrapper for repository calls: either [Success] with data or [Failure] with an [AppError]. */
sealed interface ApiResult<out T> {

    data class Success<T>(val data: T) : ApiResult<T>

    data class Failure(val error: AppError) : ApiResult<Nothing>
}
