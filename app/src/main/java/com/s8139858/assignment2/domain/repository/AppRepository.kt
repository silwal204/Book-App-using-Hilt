package com.s8139858.assignment2.domain.repository

import com.s8139858.assignment2.domain.model.DashboardData
import com.s8139858.assignment2.util.ApiResult

/**
 * Repository contract consumed by ViewModels. All suspend functions return
 * [ApiResult] so callers never have to catch exceptions.
 */
interface AppRepository {

    /** Authenticates the user and returns the keypass on success. */
    suspend fun login(username: String, password: String): ApiResult<String>

    /** Loads the dashboard entities for the given [keypass]. */
    suspend fun getDashboard(keypass: String): ApiResult<DashboardData>
}
