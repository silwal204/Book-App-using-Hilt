package com.s8139858.assignment2.data.remote.dto

import com.google.gson.annotations.SerializedName

/** Response of the auth endpoint. A null or blank keypass is treated as a malformed response. */
data class AuthResponse(
    @SerializedName("keypass") val keypass: String? = null,
)
