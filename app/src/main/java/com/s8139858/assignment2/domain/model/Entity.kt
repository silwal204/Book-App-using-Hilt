package com.s8139858.assignment2.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Domain model for one dashboard entity.
 *
 * [fields] keeps every non-description property in the original JSON order;
 * [description] is separated out so list rows never show it. Parcelable so the
 * whole entity can be passed to the Details screen via Navigation Safe Args.
 */
@Parcelize
data class Entity(
    val id: String,
    val fields: List<EntityField>,
    val description: String?,
) : Parcelable {

    /** First field value, used as the list-row title. */
    val summaryTitle: String
        get() = fields.firstOrNull()?.value ?: id

    /** Second field as "Label: value", used as the list-row subtitle. */
    val summarySubtitle: String
        get() = fields.getOrNull(1)?.let { "${'$'}{it.label}: ${'$'}{it.value}" }.orEmpty()
}
