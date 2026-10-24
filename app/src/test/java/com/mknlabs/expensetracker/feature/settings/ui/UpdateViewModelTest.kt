package com.mknlabs.expensetracker.feature.settings.ui

import android.app.Activity
import com.mknlabs.expensetracker.domain.models.UpdateConfigStatus
import com.mknlabs.expensetracker.domain.models.UpdateInfo
import com.mknlabs.expensetracker.domain.repository.UpdateRepository
import com.mknlabs.expensetracker.domain.update.PlayAppUpdateGateway
import com.mknlabs.expensetracker.domain.update.UpdateChecker
import com.mknlabs.expensetracker.utils.MainDispatcherRule
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
    private lateinit var play: FakePlayAppUpdateGateway
    private lateinit var viewModel: UpdateViewModel

    @Before
    fun setup() {
        repository = FakeUpdateRepository()
        play = FakePlayAppUpdateGateway(available = true)
        viewModel = UpdateViewModel(repository, UpdateChecker(), play)
    }

    @Test
    fun `no dialog when Play has no update`() {
        play.available = false
        viewModel.refreshPlayAvailability()

        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `optional update shows dialog when Play has a build`() {
        repository.publish(UpdateInfo(updateMessage = "Please update"))

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable)
        assertTrue(!(state as UpdateUiState.UpdateAvailable).force)
    }

    @Test
    fun `later hides optional update permanently this launch`() {
        repository.publish(UpdateInfo())
        assertTrue(viewModel.uiState.value is UpdateUiState.UpdateAvailable)

        viewModel.onLater()
        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)

        repository.publish(UpdateInfo(updateMessage = "still optional"))
        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `later cannot dismiss force update`() {
        repository.publish(UpdateInfo(forceUpdate = true))
        assertTrue(viewModel.uiState.value is UpdateUiState.UpdateAvailable)

        viewModel.onLater()

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable && state.force)
    }

    @Test
    fun `update now hides optional update`() {
        repository.publish(UpdateInfo())

        viewModel.onUpdateNow()

        assertEquals(UpdateUiState.Hidden, viewModel.uiState.value)
    }

    @Test
    fun `update now keeps force update visible`() {
        repository.publish(UpdateInfo(forceUpdate = true))

        viewModel.onUpdateNow()

        val state = viewModel.uiState.value
        assertTrue(state is UpdateUiState.UpdateAvailable && state.force)
    }

    private class FakeUpdateRepository : UpdateRepository {
        private val _updateInfo = MutableStateFlow(UpdateInfo())
        override val updateInfo: StateFlow<UpdateInfo> = _updateInfo

        override fun fetchAndActivate() = Unit

        fun publish(info: UpdateInfo) {
            _updateInfo.value = if (info.configStatus == UpdateConfigStatus.Unknown) {
                info.copy(configStatus = UpdateConfigStatus.Fetched)
            } else {
                info
            }
        }
    }

    private class FakePlayAppUpdateGateway(
        var available: Boolean
    ) : PlayAppUpdateGateway {
        override suspend fun isUpdateAvailable(): Boolean = available
        override fun startUpdate(
            activity: Activity,
            immediate: Boolean,
            onFlowUnavailable: () -> Unit
        ) = Unit
        override fun resumeInProgressUpdate(activity: Activity) = Unit
        override fun completeDownloadedUpdate() = Unit
    }
}
