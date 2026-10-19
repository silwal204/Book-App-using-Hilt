package com.s8139858.assignment2.data.mapper

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.s8139858.assignment2.data.remote.dto.DashboardResponse
import com.s8139858.assignment2.domain.model.Entity
import com.s8139858.assignment2.domain.model.EntityField
import java.util.Locale
import javax.inject.Inject

/**
 * Maps raw, order-preserving [JsonObject] entities into the domain model.
 *
 * The set of entity fields depends on the keypass and is not known at compile
 * time, so mapping is fully dynamic: every non-description property becomes an
 * [EntityField] in the original order, "description" is separated out, and the
 * mapper never crashes on missing, null, extra or nested values.
 */
class EntityMapper @Inject constructor() {

    fun mapAll(response: DashboardResponse): List<Entity> =
        response.entities.orEmpty().mapIndexed { index, json -> map(json, index) }

    fun map(json: JsonObject, fallbackIndex: Int): Entity {
        val fields = mutableListOf<EntityField>()
        var description: String? = null

        // JsonObject preserves insertion order, so field order matches the JSON.
        for ((key, element) in json.entrySet()) {
            when {
                key.equals(DESCRIPTION_KEY, ignoreCase = true) ->
                    description = element.toDisplayStringOrNull()
                key.equals(ID_KEY, ignoreCase = true) -> Unit // used as id below, not displayed
                else -> fields += EntityField(
                    label = key.toDisplayLabel(),
                    value = element.toDisplayString(),
                )
            }
        }

        val id = json.get(ID_KEY)
            ?.takeIf { it.isJsonPrimitive }
            ?.asString
            ?: fallbackIndex.toString()

        return Entity(id = id, fields = fields, description = description)
    }

    /** Nulls render as a placeholder; primitives as their natural text; nested values as compact JSON. */
    private fun JsonElement.toDisplayString(): String = when {
        isJsonNull -> NULL_PLACEHOLDER
        isJsonPrimitive -> asString
        else -> toString()
    }

    private fun JsonElement.toDisplayStringOrNull(): String? =
        if (isJsonNull) null else toDisplayString().takeIf { it.isNotBlank() }

    /** Turns camelCase / snake_case / kebab-case keys into Title Case labels. */
    private fun String.toDisplayLabel(): String =
        replace(Regex("[_\\-]+"), " ")
            .replace(Regex("(?<=[a-z0-9])(?=[A-Z])"), " ")
            .trim()
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() }
            .joinToString(" ") { word ->
                word.replaceFirstChar { first ->
                    if (first.isLowerCase()) first.titlecase(Locale.ROOT) else first.toString()
                }
            }

    private companion object {
        const val DESCRIPTION_KEY = "description"
        const val ID_KEY = "id"
        const val NULL_PLACEHOLDER = "-"
    }
}
