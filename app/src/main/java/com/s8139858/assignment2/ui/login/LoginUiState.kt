package com.s8139858.assignment2.ui.login

import androidx.annotation.StringRes

/** Render-state for the login screen. Error fields hold string resource ids. */
data class LoginUiState(
    val isLoading: Boolean = false,
    @StringRes val usernameError: Int? = null,
    @StringRes val passwordError: Int? = null,
    val showColdStartHint: Boolean = false,
)

/** One-shot events emitted by [LoginViewModel]. */
sealed interface LoginEvent {

    /** Login succeeded — navigate to the dashboard with the keypass. */
    data class NavigateToDashboard(val keypass: String) : LoginEvent

    /** Show a transient, user-friendly error message. */
    data class ShowError(@StringRes val messageRes: Int) : LoginEvent
}
