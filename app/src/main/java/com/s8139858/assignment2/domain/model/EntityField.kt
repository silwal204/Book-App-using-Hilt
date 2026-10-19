package com.s8139858.assignment2.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/** A single displayable entity attribute: a humanised [label] and its [value]. */
@Parcelize
data class EntityField(
    val label: String,
    val value: String,
) : Parcelable
