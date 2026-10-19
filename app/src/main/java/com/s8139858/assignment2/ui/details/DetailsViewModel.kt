package com.s8139858.assignment2.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.s8139858.assignment2.domain.model.Entity
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/** Holds the [Entity] handed over from the dashboard via Navigation Safe Args. */
@HiltViewModel
class DetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    val entity: Entity =
        checkNotNull(savedStateHandle["entity"]) { "DetailsViewModel requires an entity argument" }
}
