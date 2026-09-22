package com.mknlabs.expensetracker.core.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.SaverScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.mknlabs.expensetracker.models.SyncState
import com.mknlabs.expensetracker.models.Transaction

@Stable
class MainNavigationState(
    initialRoute: AppRoute = AppRoute.Home
) {
    var currentRoute by mutableStateOf(initialRoute)
        private set

    var previousRoute by mutableStateOf(AppRoute.Home)
        private set

    var profileOriginRoute by mutableStateOf(AppRoute.Home)
        private set

    var isBottomBarVisible by mutableStateOf(false)
        private set

    var selectedTransaction by mutableStateOf<Transaction?>(null)
        private set

    var addTransactionDraftAmount by mutableStateOf<String?>(null)
        private set

    var addTransactionDraftNote by mutableStateOf<String?>(null)
        private set

    var addTransactionDraftCategoryId by mutableStateOf<Int?>(null)
        private set

    var addTransactionDraftTypeId by mutableStateOf<Int?>(null)
        private set

    var addTransactionDraftAutoStartVoice by mutableStateOf(false)
        private set

    /**
     * The detected SMS the Add Transaction screen was opened from, if any.
     *
     * Held next to the draft deliberately: the draft is what that screen shows, and this is
     * what it is *for*. It survives a rotation like the draft does, so a detection being
     * reviewed is still linked to the row the user saves after the screen is recreated.
     */
    var addTransactionSourceDetectionId by mutableStateOf<String?>(null)
        private set

    var showVoiceInputSheet by mutableStateOf(false)
        private set

    fun navigateTo(route: AppRoute) {
        if (route == AppRoute.AddTransaction && currentRoute != AppRoute.ItemizedCalculator) {
            previousRoute = currentRoute
        }
        currentRoute = route
    }

    fun updateProfileOriginRoute(route: AppRoute) {
        profileOriginRoute = route
    }

    fun updateBottomBarVisibility(visible: Boolean) {
        isBottomBarVisible = visible
    }

    fun updateSelectedTransaction(transaction: Transaction?) {
        selectedTransaction = transaction
    }

    fun updateAddTransactionDraftAmount(amount: String?) {
        addTransactionDraftAmount = amount
    }

    fun updateAddTransactionDraftNote(note: String?) {
        addTransactionDraftNote = note
    }

    fun updateAddTransactionDraftCategoryId(categoryId: Int?) {
        addTransactionDraftCategoryId = categoryId
    }

    fun updateAddTransactionDraftTypeId(typeId: Int?) {
        addTransactionDraftTypeId = typeId
    }

    fun updateAddTransactionDraftAutoStartVoice(autoStart: Boolean) {
        addTransactionDraftAutoStartVoice = autoStart
    }

    fun updateAddTransactionSourceDetectionId(detectionId: String?) {
        addTransactionSourceDetectionId = detectionId
    }

    fun updateShowVoiceInputSheet(show: Boolean) {
        showVoiceInputSheet = show
    }

    fun clearTransactionDraftContext() {
        selectedTransaction = null
        addTransactionDraftAmount = null
        addTransactionDraftNote = null
        addTransactionDraftCategoryId = null
        addTransactionDraftTypeId = null
        addTransactionDraftAutoStartVoice = false
        addTransactionSourceDetectionId = null
        showVoiceInputSheet = false
    }

    /**
     * Drops only the detection a draft came from, keeping the draft itself.
     *
     * Called when the Add Transaction screen is left: a draft the user never saved must
     * not attach itself to some later, unrelated save.
     */
    fun clearAddTransactionSourceDetectionId() {
        addTransactionSourceDetectionId = null
    }

    /**
     * Restores the full navigation state after a configuration change (rotation,
     * resize, fold posture, font-size change). Called by [MainNavigationStateSaver].
     */
    internal fun restoreNavigationState(
        previous: AppRoute,
        profileOrigin: AppRoute,
        bottomBarVisible: Boolean,
        selected: Transaction?,
        draftAmount: String?,
        draftNote: String?,
        sourceDetectionId: String?
    ) {
        previousRoute = previous
        profileOriginRoute = profileOrigin
        isBottomBarVisible = bottomBarVisible
        selectedTransaction = selected
        addTransactionDraftAmount = draftAmount
        addTransactionDraftNote = draftNote
        addTransactionSourceDetectionId = sourceDetectionId
    }
}

/**
 * Round-trips the navigation state through a [Bundle]-compatible map so the user
 * is not reset to Home on rotation/resize/font-size change. [Transaction] is a
 * primitive-only data class, so it saves directly.
 */
private object MainNavigationStateSaver : Saver<MainNavigationState, Map<String, Any?>> {

    override fun SaverScope.save(value: MainNavigationState): Map<String, Any?>? {
        return mapOf(
            "currentRoute" to value.currentRoute.route,
            "previousRoute" to value.previousRoute.route,
            "profileOriginRoute" to value.profileOriginRoute.route,
            "isBottomBarVisible" to value.isBottomBarVisible,
            "selectedTransaction" to value.selectedTransaction?.let { tx ->
                mapOf(
                    "id" to tx.id,
                    "note" to tx.note,
                    "createdAt" to tx.createdAt,
                    "amountMinor" to tx.amountMinor,
                    "transactionTypeId" to tx.transactionTypeId,
                    "paymentTypeId" to tx.paymentTypeId,
                    "categoryId" to tx.categoryId,
                    "contentHash" to tx.contentHash,
                    "syncState" to tx.syncState.name,
                    "isDeleted" to tx.isDeleted,
                    "updatedAt" to tx.updatedAt,
                    "sourceRecurringRuleId" to tx.sourceRecurringRuleId
                )
            },
            "addTransactionDraftAmount" to value.addTransactionDraftAmount,
            "addTransactionDraftNote" to value.addTransactionDraftNote,
            "addTransactionSourceDetectionId" to value.addTransactionSourceDetectionId
        )
    }

    override fun restore(value: Map<String, Any?>): MainNavigationState? {
        val state = MainNavigationState(
            initialRoute = AppRoute.fromRoute(value["currentRoute"] as? String) ?: AppRoute.Home
        )
        val selected = (value["selectedTransaction"] as? Map<*, *>)?.let { m ->
            Transaction(
                id = m["id"] as? String ?: return@let null,
                note = m["note"] as? String ?: "",
                createdAt = m["createdAt"] as? Long ?: 0L,
                amountMinor = m["amountMinor"] as? Long ?: 0L,
                transactionTypeId = m["transactionTypeId"] as? Int ?: 0,
                paymentTypeId = m["paymentTypeId"] as? Int ?: 0,
                categoryId = m["categoryId"] as? Int ?: 0,
                contentHash = m["contentHash"] as? String,
                syncState = runCatching {
                    SyncState.valueOf(m["syncState"] as? String ?: SyncState.PENDING_UPLOAD.name)
                }.getOrDefault(SyncState.PENDING_UPLOAD),
                isDeleted = m["isDeleted"] as? Boolean ?: false,
                updatedAt = m["updatedAt"] as? Long ?: (m["createdAt"] as? Long ?: 0L),
                sourceRecurringRuleId = m["sourceRecurringRuleId"] as? String
            )
        }
        state.restoreNavigationState(
            previous = AppRoute.fromRoute(value["previousRoute"] as? String) ?: AppRoute.Home,
            profileOrigin = AppRoute.fromRoute(value["profileOriginRoute"] as? String) ?: AppRoute.Home,
            bottomBarVisible = value["isBottomBarVisible"] as? Boolean ?: false,
            selected = selected,
            draftAmount = value["addTransactionDraftAmount"] as? String,
            draftNote = value["addTransactionDraftNote"] as? String,
            sourceDetectionId = value["addTransactionSourceDetectionId"] as? String
        )
        return state
    }
}

@Composable
fun rememberMainNavigationState(
    initialRoute: AppRoute = AppRoute.Home
): MainNavigationState {
    return rememberSaveable(saver = MainNavigationStateSaver) {
        MainNavigationState(initialRoute = initialRoute)
    }
}
