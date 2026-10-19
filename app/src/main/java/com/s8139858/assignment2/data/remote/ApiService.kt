package com.s8139858.assignment2.data.remote

import com.s8139858.assignment2.data.remote.dto.AuthRequest
import com.s8139858.assignment2.data.remote.dto.AuthResponse
import com.s8139858.assignment2.data.remote.dto.DashboardResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Retrofit contract for the nit3213 API.
 *
 * Retrofit is a type-safe HTTP client: we declare endpoints as annotated
 * Kotlin functions in an interface and Retrofit generates the implementation
 * at runtime — no manual URL building or connection handling.
 * `suspend` marks these as coroutine functions, so calls run off the main
 * thread without callbacks.
 */
interface ApiService {

    // @Path values are substituted into the URL at runtime (campus = "sydney");
    // @Body serialises the DTO to the JSON request body via Gson.
    @POST("{campus}/auth")
    suspend fun login(
        @Path("campus") campus: String,
        @Body request: AuthRequest,
    ): AuthResponse

    @GET("dashboard/{keypass}")
    suspend fun getDashboard(
        @Path("keypass") keypass: String,
    ): DashboardResponse
}
