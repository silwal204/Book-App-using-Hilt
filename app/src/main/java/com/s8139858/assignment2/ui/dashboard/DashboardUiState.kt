package com.s8139858.assignment2.ui.dashboard

import androidx.annotation.StringRes
import com.s8139858.assignment2.domain.model.Entity

/** Render-state for the dashboard screen. */
sealed interface DashboardUiState {

    data object Loading : DashboardUiState

    data class Success(
        val entities: List<Entity>,
        val entityTotal: Int,
    ) : DashboardUiState

    data object Empty : DashboardUiState

    data class Error(@StringRes val messageRes: Int) : DashboardUiState
}
