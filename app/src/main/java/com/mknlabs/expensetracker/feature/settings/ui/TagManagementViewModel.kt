package com.mknlabs.expensetracker.feature.settings.ui

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.TagRepository
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.TagStats
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * One tag as the management screen renders it: the tag itself plus its usage.
 *
 * [transactionCount] and [expenseMinor] come from the stats stream, so a tag created a
 * moment ago appears immediately with zeros rather than being absent until used.
 */
@Immutable
data class TagManagementItemUi(
    val tag: Tag,
    val transactionCount: Int,
    val expenseMinor: Long
) {
    val id: String get() = tag.id
    val name: String get() = tag.name
    val colorHex: String? get() = tag.colorHex
}

@Immutable
data class TagManagementUiState(
    val items: List<TagManagementItemUi> = emptyList(),
    /** True while the user is picking a second tag to merge the first into. */
    val mergeSourceId: String? = null,
    /** A transient message (a merge that was refused, a rename that clashed). */
    val message: String? = null
) {
    val isMerging: Boolean get() = mergeSourceId != null
    val mergeSource: TagManagementItemUi? get() = items.firstOrNull { it.id == mergeSourceId }
}

/**
 * Backs the Tag Management screen.
 *
 * The list is a `combine` of the tags and their statistics rather than two separately
 * collected flows, so the count shown beside a tag and the tag itself can never come
 * from different moments — a rename lands on the row that also carries its usage.
 */
@HiltViewModel
class TagManagementViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TagManagementUiState())
    val uiState: StateFlow<TagManagementUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                tagRepository.observeActiveTags(),
                tagRepository.observeTagStats()
            ) { tags, stats -> tags to stats }
                .collect { (tags, stats) ->
                    _uiState.update { current ->
                        current.copy(
                            items = tags.map { tag ->
                                val usage = stats[tag.id]
                                TagManagementItemUi(
                                    tag = tag,
                                    transactionCount = usage?.transactionCount ?: 0,
                                    expenseMinor = usage?.expenseMinor ?: 0L
                                )
                            }
                        )
                    }
                }
        }
    }

    fun createTag(name: String, colorHex: String? = null) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { tagRepository.createTag(trimmed, colorHex) }
    }

    fun renameTag(id: String, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch { tagRepository.renameTag(id, trimmed) }
    }

    fun updateColor(id: String, colorHex: String?) {
        viewModelScope.launch { tagRepository.updateTagColor(id, colorHex) }
    }

    fun deleteTag(id: String) {
        viewModelScope.launch {
            tagRepository.deleteTag(id)
            _uiState.update { it.copy(mergeSourceId = it.mergeSourceId.takeIf { s -> s != id }) }
        }
    }

    /**
     * Starts a merge from [id], or completes it when one is already pending.
     *
     * A two-tap flow rather than a dialog: the first tap arms a source, the second
     * names the target, and the banner tells the user which tag is being merged away.
     * That keeps the whole interaction on the list the user is looking at, which is
     * where the decision is actually being made.
     */
    fun toggleMerge(id: String) {
        val current = _uiState.value
        val source = current.mergeSourceId
        when {
            source == null -> _uiState.update { it.copy(mergeSourceId = id, message = null) }
            source == id -> _uiState.update { it.copy(mergeSourceId = null) }
            else -> viewModelScope.launch {
                tagRepository.mergeTags(sourceId = source, targetId = id)
                _uiState.update { it.copy(mergeSourceId = null) }
            }
        }
    }

    fun cancelMerge() {
        _uiState.update { it.copy(mergeSourceId = null) }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}