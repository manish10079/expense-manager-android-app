package com.mknlabs.expensetracker.ui.viewmodels

import com.google.android.ump.ConsentInformation.PrivacyOptionsRequirementStatus
import com.google.firebase.auth.FirebaseUser
import com.mknlabs.expensetracker.domain.repository.AuthRepository
import com.mknlabs.expensetracker.domain.repository.ConfigurationRepository
import com.mknlabs.expensetracker.domain.repository.MonetizationRepository
import com.mknlabs.expensetracker.domain.repository.RegisteredDevice
import com.mknlabs.expensetracker.domain.repository.SyncRepository
import com.mknlabs.expensetracker.models.UserProfile
import com.mknlabs.expensetracker.models.UserTier
import com.mknlabs.expensetracker.monetization.AccessStatus
import com.mknlabs.expensetracker.monetization.AdsCoordinator
import com.mknlabs.expensetracker.monetization.Feature
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import androidx.lifecycle.viewModelScope
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private lateinit var viewModel: SettingsViewModel
    private lateinit var adsCoordinator: AdsCoordinator

    @Before
    fun setup() {
        adsCoordinator = AdsCoordinator(android.app.Application())
        viewModel = SettingsViewModel(
            authRepository = FakeAuthRepository(),
            monetizationRepository = FakeMonetizationRepository(),
            configurationRepository = FakeConfigurationRepository(),
            syncRepository = FakeSyncRepository(),
            adsCoordinator = adsCoordinator
        )

        // Start collecting the flow to keep it active during tests.
        viewModel.viewModelScope.launch(UnconfinedTestDispatcher()) {
            viewModel.uiState.collect { }
        }
    }

    @Test
    fun `privacy options item hidden while UMP status is UNKNOWN`() {
        // The status flow starts UNKNOWN (nothing published yet) -> entry point hidden.
        awaitUiState { viewModel.uiState.value.settingsSections.isNotEmpty() }
        assertFalse(hasPrivacyOptionsItem(viewModel))
    }

    @Test
    fun `privacy options item shown when UMP status is REQUIRED`() {
        adsCoordinator.publishPrivacyOptionsRequirementStatusForTesting(
            PrivacyOptionsRequirementStatus.REQUIRED
        )

        awaitUiState { hasPrivacyOptionsItem(viewModel) }
    }

    @Test
    fun `privacy options item hides again when UMP status changes from REQUIRED to NOT_REQUIRED`() {
        adsCoordinator.publishPrivacyOptionsRequirementStatusForTesting(
            PrivacyOptionsRequirementStatus.REQUIRED
        )
        awaitUiState { hasPrivacyOptionsItem(viewModel) }

        adsCoordinator.publishPrivacyOptionsRequirementStatusForTesting(
            PrivacyOptionsRequirementStatus.NOT_REQUIRED
        )

        awaitUiState { !hasPrivacyOptionsItem(viewModel) }
    }

    private fun hasPrivacyOptionsItem(vm: SettingsViewModel): Boolean {
        return vm.uiState.value.settingsSections
            .flatMap { it.items }
            .any { it.actionId == SettingsActionId.PrivacyOptions }
    }

    private fun awaitUiState(timeoutMs: Long = 5_000, condition: () -> Boolean) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            if (condition()) return
            Thread.sleep(10)
        }
        assertTrue("UI state condition not met within ${timeoutMs}ms", condition())
    }

    private class FakeAuthRepository : AuthRepository {
        override val currentUser: StateFlow<FirebaseUser?> = MutableStateFlow(null)
        override suspend fun signInWithGoogle(idToken: String): Result<Boolean> = Result.success(false)
        override suspend fun signInWithEmail(email: String, password: String): Result<Boolean> = Result.success(false)
        override suspend fun signUpWithEmail(email: String, password: String): Result<Boolean> = Result.success(false)
        override suspend fun sendPasswordResetEmail(email: String): Result<Unit> = Result.success(Unit)
        override suspend fun signInAnonymously(): Result<Boolean> = Result.success(false)
        override suspend fun deleteAccount(): Result<Unit> = Result.success(Unit)
        override suspend fun sendMagicLink(email: String): Result<Unit> = Result.success(Unit)
        override suspend fun completeSignInWithLink(email: String, emailLink: String): Result<Boolean> = Result.success(false)
        override fun isSignInWithEmailLink(link: String): Boolean = false
        override fun signOut() {}
        override fun isUserLoggedIn(): Boolean = false
        override suspend fun sendEmailVerification(): Result<Unit> = Result.success(Unit)
        override suspend fun reloadUser(): Result<Unit> = Result.success(Unit)
        override suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit> = Result.success(Unit)
        override suspend fun verifyBeforeUpdateEmail(newEmail: String): Result<Unit> = Result.success(Unit)
        override suspend fun reauthenticate(email: String, password: String): Result<Unit> = Result.success(Unit)
    }

    private class FakeMonetizationRepository : MonetizationRepository {
        override val userTier: Flow<UserTier> = flowOf(UserTier.FREE)
        override val isAdsEnabled: Flow<Boolean> = flowOf(true)
        override val globalAdAccessExpiry: Flow<Long> = flowOf(0L)
        override fun observeAccessStatus(feature: Feature, optionId: String?): Flow<AccessStatus> =
            flowOf(AccessStatus.Granted)
        override suspend fun grantTemporaryAccess(feature: Feature, optionId: String?, durationMillis: Long) {}
        override suspend fun becomePremium() {}
    }

    private class FakeConfigurationRepository : ConfigurationRepository {
        override val minRequiredVersion: StateFlow<Int> = MutableStateFlow(0)
        override val isUnderMaintenance: StateFlow<Boolean> = MutableStateFlow(false)
        override val currentPromoCode: StateFlow<String> = MutableStateFlow("")
        override val isProPassEnabled: StateFlow<Boolean> = MutableStateFlow(false)
        override val isSyncEnabled: StateFlow<Boolean> = MutableStateFlow(false)
        override val maxSyncDevices: StateFlow<Int> = MutableStateFlow(0)
        override val googleSheetsFeedbackUrl: StateFlow<String> = MutableStateFlow("")
        override fun fetchAndActivate() {}
        override fun isUpdateRequired(): Boolean = false
    }

    private class FakeSyncRepository : SyncRepository {
        override val registeredDevices: StateFlow<List<RegisteredDevice>> = MutableStateFlow(emptyList())
        override val isSyncEnabled: StateFlow<Boolean> = MutableStateFlow(false)
        override val isSyncing: StateFlow<Boolean> = MutableStateFlow(false)
        override val lastSyncTimeMillis: StateFlow<Long> = MutableStateFlow(0L)
        override suspend fun registerCurrentDevice(): Result<Unit> = Result.success(Unit)
        override suspend fun unregisterDevice(deviceId: String): Result<Unit> = Result.success(Unit)
        override suspend fun refreshDevices(): Result<Unit> = Result.success(Unit)
        override suspend fun syncUserProfile(isNewUser: Boolean): Result<Unit> = Result.success(Unit)
        override suspend fun syncTransactions(): Result<Unit> = Result.success(Unit)
        override suspend fun fetchUserProfileFromCloud(uid: String): UserProfile? = null
        override suspend fun forceSyncTransactions(): Result<Unit> = Result.success(Unit)
    }
}
