package com.s8139858.assignment2.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s8139858.assignment2.domain.repository.AppRepository
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Loads the dashboard once per ViewModel instance, so configuration changes
 * (rotation) never trigger a refetch. [retry] reloads after an error.
 */
@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: AppRepository,
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val keypass: String =
        checkNotNull(savedStateHandle["keypass"]) { "DashboardViewModel requires a keypass argument" }

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        // Loading here (once per ViewModel instance) is why rotation never refetches:
        // a configuration change recreates the Fragment but keeps this ViewModel alive.
        loadDashboard()
    }

    fun retry() {
        loadDashboard()
    }

    private fun loadDashboard() {
        viewModelScope.launch {
            _uiState.value = DashboardUiState.Loading
            when (val result = repository.getDashboard(keypass)) {
                is ApiResult.Success -> {
                    val data = result.data
                    _uiState.value = if (data.entities.isEmpty()) {
                        DashboardUiState.Empty
                    } else {
                        DashboardUiState.Success(
                            entities = data.entities,
                            entityTotal = data.entityTotal,
                        )
                    }
                }
                is ApiResult.Failure ->
                    _uiState.value = DashboardUiState.Error(result.error.toMessageRes())
            }
        }
    }
}
