package com.s8139858.assignment2.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * Login payload. The username is always a [String] so that a leading zero
 * in a student number is preserved end to end.
 */
data class AuthRequest(
    @SerializedName("username") val username: String,
    @SerializedName("password") val password: String,
)
