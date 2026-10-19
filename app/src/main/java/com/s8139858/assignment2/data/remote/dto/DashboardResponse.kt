package com.s8139858.assignment2.data.remote.dto

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName

/**
 * Response of the dashboard endpoint.
 *
 * Entity fields are not fixed — they depend on the keypass/topic — so each
 * entity is parsed as an order-preserving [JsonObject] and mapped to a
 * domain model by the EntityMapper.
 */
data class DashboardResponse(
    @SerializedName("entities") val entities: List<JsonObject>? = emptyList(),
    @SerializedName("entityTotal") val entityTotal: Int? = 0,
)
