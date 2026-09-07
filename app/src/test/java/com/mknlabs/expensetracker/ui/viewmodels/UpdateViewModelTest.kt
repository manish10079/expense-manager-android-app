package com.mknlabs.expensetracker.ui.viewmodels

import com.mknlabs.expensetracker.BuildConfig
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.domain.repository.UpdateRepository
import com.mknlabs.expensetracker.domain.update.UpdateChecker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class UpdateViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var repository: FakeUpdateRepository
    private lateinit var viewModel: UpdateViewModel

    @Before
    fun setup() {
        repository = FakeUpdateRepository()
        viewModel = UpdateViewModel(repository, UpdateChecker())
    }

    @Test
    fun `no dialog when installed version is current`() {
        repository.publish(UpdateInfo(latestVersionCode = BuildConfig.VERSION_CODE))

        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `optional update shows dialog`() {
        repository.publish(UpdateInfo(latestVersionCode = BuildConfig.VERSION_CODE + 1))

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable)
        assertTrue(!(state as UpdateUiState.UpdateAvailable).force)
    }

    @Test
    fun `later hides optional update permanently this launch`() {
        repository.publish(UpdateInfo(latestVersionCode = BuildConfig.VERSION_CODE + 1))
        assertTrue(viewModel.uiState.value is UpdateUiState.UpdateAvailable)

        viewModel.onLater()
        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)

        // A later Remote Config refresh must not re-show it in the same launch.
        repository.publish(UpdateInfo(latestVersionCode = BuildConfig.VERSION_CODE + 2))
        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `later cannot dismiss force update`() {
        repository.publish(
            UpdateInfo(
                latestVersionCode = BuildConfig.VERSION_CODE + 1,
                forceUpdate = true
            )
        )
        assertTrue(viewModel.uiState.value is UpdateUiState.UpdateAvailable)

        viewModel.onLater()

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable && state.force)
    }

    @Test
    fun `update now hides optional update`() {
        repository.publish(UpdateInfo(latestVersionCode = BuildConfig.VERSION_CODE + 1))

        viewModel.onUpdateNow()

        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `update now keeps force update visible`() {
        repository.publish(
            UpdateInfo(
                latestVersionCode = BuildConfig.VERSION_CODE + 1,
                forceUpdate = true
            )
        )

        viewModel.onUpdateNow()

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable && state.force)
    }

    private class FakeUpdateRepository : UpdateRepository {
        private val _updateInfo = MutableStateFlow(UpdateInfo())
        override val updateInfo: StateFlow<UpdateInfo> = _updateInfo

        override fun fetchAndActivate() = Unit

        fun publish(info: UpdateInfo) {
            _updateInfo.value = info
        }
    }
}