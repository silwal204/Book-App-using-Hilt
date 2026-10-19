package com.s8139858.assignment2.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.s8139858.assignment2.R
import com.s8139858.assignment2.domain.repository.AppRepository
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.toMessageRes
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Validates credentials locally, authenticates via [AppRepository] and exposes
 * render-state plus one-shot events. The username is treated as a [String]
 * everywhere so a leading zero survives the round trip.
 */
@HiltViewModel
class LoginViewModel @Inject constructor(
    private val repository: AppRepository,
) : ViewModel() {

    // Kotlin Flow: StateFlow is a hot stream that always holds the latest render-state;
    // the Fragment collects it and re-renders on every emission. It also survives
    // rotation because the ViewModel outlives the Fragment view.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // One-shot events (navigation, snackbars) use a Channel-backed Flow so they are
    // delivered exactly once, unlike StateFlow which would re-deliver on rotation.
    private val _events = Channel<LoginEvent>(Channel.BUFFERED)
    val events: Flow<LoginEvent> = _events.receiveAsFlow()

    private var loginJob: Job? = null

    fun login(username: String, password: String) {
        val trimmedUsername = username.trim()
        val usernameError = if (trimmedUsername.isEmpty()) R.string.error_username_required else null
        val passwordError = if (password.isEmpty()) R.string.error_password_required else null
        if (usernameError != null || passwordError != null) {
            _uiState.update { it.copy(usernameError = usernameError, passwordError = passwordError) }
            return
        }
        if (loginJob?.isActive == true) return // prevent double submits

        // viewModelScope: the coroutine is cancelled automatically when the ViewModel
        // is cleared — no GlobalScope, so nothing leaks past the screen's lifetime.
        loginJob = viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    usernameError = null,
                    passwordError = null,
                    showColdStartHint = false,
                )
            }
            // The free-tier Render server can take 30-60s to wake up; hint after ~5s.
            val coldStartJob = launch {
                delay(COLD_START_HINT_DELAY_MS)
                _uiState.update { state ->
                    if (state.isLoading) state.copy(showColdStartHint = true) else state
                }
            }
            try {
                when (val result = repository.login(trimmedUsername, password)) {
                    is ApiResult.Success -> {
                        _uiState.update { it.copy(isLoading = false, showColdStartHint = false) }
                        _events.send(LoginEvent.NavigateToDashboard(result.data))
                    }
                    is ApiResult.Failure -> {
                        _uiState.update { it.copy(isLoading = false, showColdStartHint = false) }
                        _events.send(LoginEvent.ShowError(result.error.toMessageRes()))
                    }
                }
            } finally {
                coldStartJob.cancel()
            }
        }
    }

    /** Clears the username field error once the user starts typing again. */
    fun onUsernameChanged() {
        if (_uiState.value.usernameError != null) {
            _uiState.update { it.copy(usernameError = null) }
        }
    }

    /** Clears the password field error once the user starts typing again. */
    fun onPasswordChanged() {
        if (_uiState.value.passwordError != null) {
            _uiState.update { it.copy(passwordError = null) }
        }
    }

    private companion object {
        const val COLD_START_HINT_DELAY_MS = 5_000L
    }
}
