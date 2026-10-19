package com.s8139858.assignment2.ui.details

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.s8139858.assignment2.domain.model.Entity
import com.s8139858.assignment2.domain.model.EntityField
import org.junit.Test

class DetailsViewModelTest {

    private val entity = Entity(
        id = "0",
        fields = listOf(
            EntityField("Title", "The Hobbit"),
            EntityField("Author", "J.R.R. Tolkien"),
        ),
        description = "A fantasy novel.",
    )

    @Test
    fun `given entity argument when viewmodel created then entity is exposed`() {
        val viewModel = DetailsViewModel(SavedStateHandle(mapOf("entity" to entity)))

        assertThat(viewModel.entity).isEqualTo(entity)
    }

    @Test(expected = IllegalStateException::class)
    fun `given missing entity argument when viewmodel created then it fails fast`() {
        DetailsViewModel(SavedStateHandle())
    }
}
