package com.s8139858.assignment2.ui.login

import app.cash.turbine.test
import com.google.common.truth.Truth.assertThat
import com.s8139858.assignment2.R
import com.s8139858.assignment2.domain.repository.AppRepository
import com.s8139858.assignment2.util.AppError
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests for LoginViewModel — JVM-only (src/test), JUnit4 + MockK + Turbine.
 *
 * This class is the payoff of dependency injection: the ViewModel depends on the
 * AppRepository *interface*, so MockK supplies a fake and no network ever runs.
 * (MockK is the Kotlin-first equivalent of Mockito — same mocking role.)
 * Turbine collects the Flow of one-shot events so emissions can be asserted.
 */
class LoginViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AppRepository = mockk()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setUp() {
        viewModel = LoginViewModel(repository)
    }

    @Test
    fun `given empty username when login then username error shown and repository not called`() = runTest {
        viewModel.login(username = "", password = "Sulav")

        assertThat(viewModel.uiState.value.usernameError).isEqualTo(R.string.error_username_required)
        assertThat(viewModel.uiState.value.isLoading).isFalse()
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `given empty password when login then password error shown and repository not called`() = runTest {
        viewModel.login(username = "08139858", password = "")

        assertThat(viewModel.uiState.value.passwordError).isEqualTo(R.string.error_password_required)
        coVerify(exactly = 0) { repository.login(any(), any()) }
    }

    @Test
    fun `given valid credentials when login succeeds then dashboard event carries keypass`() = runTest {
        coEvery { repository.login("08139858", "Sulav") } returns ApiResult.Success("books")

        viewModel.events.test {
            viewModel.login("08139858", "Sulav")

            assertThat(awaitItem()).isEqualTo(LoginEvent.NavigateToDashboard("books"))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `given username with leading zero when login then leading zero is preserved`() = runTest {
        coEvery { repository.login(any(), any()) } returns ApiResult.Success("books")

        viewModel.login("08139858", "Sulav")

        coVerify(exactly = 1) { repository.login("08139858", "Sulav") }
    }

    @Test
    fun `given invalid credentials error when login then error event with invalid credentials message`() = runTest {
        coEvery { repository.login(any(), any()) } returns ApiResult.Failure(AppError.InvalidCredentials)

        viewModel.events.test {
            viewModel.login("08139858", "WrongName")

            assertThat(awaitItem()).isEqualTo(LoginEvent.ShowError(R.string.error_invalid_credentials))
            cancelAndIgnoreRemainingEvents()
        }
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }

    @Test
    fun `given no network error when login then error event with no internet message`() = runTest {
        coEvery { repository.login(any(), any()) } returns ApiResult.Failure(AppError.NoNetwork)

        viewModel.events.test {
            viewModel.login("08139858", "Sulav")

            assertThat(awaitItem()).isEqualTo(LoginEvent.ShowError(R.string.error_no_internet))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `given timeout error when login then error event with timeout message`() = runTest {
        coEvery { repository.login(any(), any()) } returns ApiResult.Failure(AppError.Timeout)

        viewModel.events.test {
            viewModel.login("08139858", "Sulav")

            assertThat(awaitItem()).isEqualTo(LoginEvent.ShowError(R.string.error_timeout))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `when login runs then loading state toggles on and off`() = runTest {
        // Gate the repository so the loading phase is observable: StateFlow
        // conflates intermediate values when the call completes synchronously.
        val gate = kotlinx.coroutines.CompletableDeferred<ApiResult<String>>()
        coEvery { repository.login(any(), any()) } coAnswers { gate.await() }

        viewModel.login("08139858", "Sulav")
        assertThat(viewModel.uiState.value.isLoading).isTrue()

        gate.complete(ApiResult.Success("books"))
        assertThat(viewModel.uiState.value.isLoading).isFalse()
    }
}
