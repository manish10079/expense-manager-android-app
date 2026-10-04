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
    /** A transient message (a merge that was refused, a rename that clashed). */
    val message: String? = null
)

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

    /**
     * Soft-deletes every tag in [ids]. Each one is detached from its transactions first,
     * so a bulk delete is only a loop over the single-tag operation and cannot leave a
     * tag half-removed.
     */
    fun deleteTags(ids: Collection<String>) {
        val targets = ids.toList()
        if (targets.isEmpty()) return
        viewModelScope.launch {
            targets.forEach { tagRepository.deleteTag(it) }
        }
    }

    /**
     * Merges every id in [sourceIds] into [targetId], one source at a time.
     *
     * Sequential single merges rather than a new bulk SQL path: each merge already
     * re-points the source's links, collapses duplicates on the target and tombstones
     * the source in one transaction, so doing them in order produces exactly the state
     * a single merge of all the sources would. The target survives; every source is
     * soft-deleted.
     */
    fun mergeTags(targetId: String, sourceIds: Collection<String>) {
        val sources = sourceIds.filter { it != targetId }
        if (sources.isEmpty()) return
        viewModelScope.launch {
            sources.forEach { source ->
                tagRepository.mergeTags(sourceId = source, targetId = targetId)
            }
        }
    }

    fun clearMessage() {
        _uiState.update { it.copy(message = null) }
    }
}
