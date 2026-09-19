package com.mknlabs.expensetracker.feature.smsinbox.domain.usecase

import com.mknlabs.expensetracker.feature.smsinbox.domain.model.DetectedSmsNotification
import com.mknlabs.expensetracker.feature.smsinbox.domain.model.SmsInboxFilter
import com.mknlabs.expensetracker.feature.smsinbox.domain.repository.SmsInboxRepository
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

/**
 * Read side of the inbox. Every screen goes through these rather than touching the
 * repository directly, so the filter/search rules live in one place.
 */

/** Reactive inbox stream for one filter + search term. */
class ObserveSmsInboxUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    operator fun invoke(
        filter: SmsInboxFilter,
        searchQuery: String? = null,
        limit: Int = SmsInboxRepository.DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): Flow<List<DetectedSmsNotification>> = repository.observeInbox(filter, searchQuery, limit, offset)
}

/** One page, for the append path of infinite scrolling. */
class GetSmsInboxPageUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    suspend operator fun invoke(
        filter: SmsInboxFilter,
        searchQuery: String? = null,
        limit: Int = SmsInboxRepository.DEFAULT_PAGE_SIZE,
        offset: Int = 0
    ): List<DetectedSmsNotification> = repository.getPage(filter, searchQuery, limit, offset)
}

/** Unread count behind the Home bell badge. */
class GetSmsInboxUnreadCountUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    operator fun invoke(): Flow<Int> = repository.observeUnreadCount()
}

/** Fetches detections by id — used by the review sheet and notification actions. */
class GetSmsDetectionsUseCase @Inject constructor(
    private val repository: SmsInboxRepository
) {
    suspend operator fun invoke(ids: List<String>): List<DetectedSmsNotification> =
        repository.getByIds(ids)

    suspend operator fun invoke(id: String): DetectedSmsNotification? = repository.getById(id)
}
