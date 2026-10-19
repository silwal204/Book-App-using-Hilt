package com.s8139858.assignment2.ui.dashboard

import androidx.lifecycle.SavedStateHandle
import com.google.common.truth.Truth.assertThat
import com.s8139858.assignment2.R
import com.s8139858.assignment2.domain.model.DashboardData
import com.s8139858.assignment2.domain.model.Entity
import com.s8139858.assignment2.domain.model.EntityField
import com.s8139858.assignment2.domain.repository.AppRepository
import com.s8139858.assignment2.util.ApiResult
import com.s8139858.assignment2.util.AppError
import com.s8139858.assignment2.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class DashboardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repository: AppRepository = mockk()

    private val sampleEntity = Entity(
        id = "0",
        fields = listOf(
            EntityField("Title", "1984"),
            EntityField("Author", "George Orwell"),
        ),
        description = "A dystopian novel.",
    )

    private fun createViewModel(): DashboardViewModel =
        DashboardViewModel(repository, SavedStateHandle(mapOf("keypass" to "books")))

    @Test
    fun `given entities when dashboard loads then success state exposes list and total`() = runTest {
        coEvery { repository.getDashboard("books") } returns
            ApiResult.Success(DashboardData(entities = listOf(sampleEntity), entityTotal = 7))

        val state = createViewModel().uiState.value

        assertThat(state).isInstanceOf(DashboardUiState.Success::class.java)
        val success = state as DashboardUiState.Success
        assertThat(success.entities).containsExactly(sampleEntity)
        assertThat(success.entityTotal).isEqualTo(7)
    }

    @Test
    fun `given empty entity list when dashboard loads then empty state shown`() = runTest {
        coEvery { repository.getDashboard("books") } returns
            ApiResult.Success(DashboardData(entities = emptyList(), entityTotal = 0))

        assertThat(createViewModel().uiState.value).isEqualTo(DashboardUiState.Empty)
    }

    @Test
    fun `given error then success when retry then state recovers and repository called twice`() = runTest {
        coEvery { repository.getDashboard("books") } returnsMany listOf(
            ApiResult.Failure(AppError.NoNetwork),
            ApiResult.Success(DashboardData(entities = listOf(sampleEntity), entityTotal = 7)),
        )
        val viewModel = createViewModel()

        val error = viewModel.uiState.value
        assertThat(error).isInstanceOf(DashboardUiState.Error::class.java)
        assertThat((error as DashboardUiState.Error).messageRes).isEqualTo(R.string.error_no_internet)

        viewModel.retry()

        assertThat(viewModel.uiState.value).isInstanceOf(DashboardUiState.Success::class.java)
        coVerify(exactly = 2) { repository.getDashboard("books") }
    }

    @Test
    fun `when dashboard is loading then state stays loading until the call completes`() = runTest {
        val gate = CompletableDeferred<ApiResult<DashboardData>>()
        coEvery { repository.getDashboard("books") } coAnswers { gate.await() }
        val viewModel = createViewModel()

        assertThat(viewModel.uiState.value).isEqualTo(DashboardUiState.Loading)

        gate.complete(ApiResult.Success(DashboardData(entities = listOf(sampleEntity), entityTotal = 7)))

        assertThat(viewModel.uiState.value).isInstanceOf(DashboardUiState.Success::class.java)
    }

    @Test
    fun `given server error when dashboard loads then error state with server message`() = runTest {
        coEvery { repository.getDashboard("books") } returns ApiResult.Failure(AppError.Server(500))

        val state = createViewModel().uiState.value

        assertThat(state).isInstanceOf(DashboardUiState.Error::class.java)
        assertThat((state as DashboardUiState.Error).messageRes).isEqualTo(R.string.error_server)
    }
}
