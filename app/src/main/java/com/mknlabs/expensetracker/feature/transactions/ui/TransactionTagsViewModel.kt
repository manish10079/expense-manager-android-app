package com.mknlabs.expensetracker.feature.transactions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mknlabs.expensetracker.domain.repository.TagRepository
import com.mknlabs.expensetracker.models.Tag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * The tag side of the Add/Edit Transaction screen: the catalogue to suggest from, and
 * the set of tags the user has chosen for this transaction.
 *
 * Kept apart from the screen's other state because it is the one part of that form that
 * has to talk to a repository. The screen renders chips and a suggestion list; this
 * holds the selection and turns a typed name into a tag.
 *
 * The selection is held as ids, not [Tag] values, so a rename or recolour performed
 * elsewhere updates the chip here without the screen having to reconcile two copies of
 * the same tag. [selectedTags] joins the ids back against the live catalogue for
 * display, which is why a tag deleted on the management screen simply drops out.
 */
@HiltViewModel
class TransactionTagsViewModel @Inject constructor(
    private val tagRepository: TagRepository
) : ViewModel() {

    /** Every live tag, for the suggestion dropdown. */
    val allTags: StateFlow<List<Tag>> = tagRepository.observeActiveTags()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    private val _selectedTagIds = MutableStateFlow<List<String>>(emptyList())

    /** The tags currently applied to the draft, in the order they were chosen. */
    val selectedTags: StateFlow<List<Tag>> =
        combine(allTags, _selectedTagIds) { catalogue, ids ->
            ids.mapNotNull { id -> catalogue.firstOrNull { it.id == id } }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    /**
     * Seeds the selection for edit mode.
     *
     * Guarded by [seeded] so the initial load cannot overwrite edits the user has already
     * made: the ids arrive asynchronously from the database, and without the guard a slow
     * read would land after the user added a tag and silently reset the field.
     */
    private var seeded = false

    fun seedFromTransaction(transactionId: String) {
        if (seeded || transactionId.isBlank()) return
        viewModelScope.launch {
            val ids = tagRepository.getTagIdsForTransaction(transactionId)
            if (!seeded) {
                seeded = true
                _selectedTagIds.value = ids
            }
        }
    }

    /** The ids to persist; read by the Route at save time. */
    val selectedTagIds: List<String> get() = _selectedTagIds.value

    /** Attaches an existing tag, or detaches it if it is already selected. */
    fun toggleTag(tagId: String) {
        seeded = true
        _selectedTagIds.value = _selectedTagIds.value.let { current ->
            if (tagId in current) current - tagId else current + tagId
        }
    }

    fun removeTag(tagId: String) {
        seeded = true
        _selectedTagIds.value = _selectedTagIds.value - tagId
    }

    /**
     * Turns a typed name into a tag and attaches it.
     *
     * Creation goes through the repository, which returns the existing tag when the name
     * already matches case-insensitively — so this covers both "pick the tag that already
     * exists" and "make a new one", and the screen does not have to decide which it is
     * before calling.
     */
    fun addTagByName(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        seeded = true
        viewModelScope.launch {
            val tag = tagRepository.createTag(trimmed)
            if (tag.id !in _selectedTagIds.value) {
                _selectedTagIds.value = _selectedTagIds.value + tag.id
            }
        }
    }
}