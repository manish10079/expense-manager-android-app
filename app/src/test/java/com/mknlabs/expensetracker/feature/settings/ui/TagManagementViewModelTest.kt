package com.mknlabs.expensetracker.feature.settings.ui

import com.mknlabs.expensetracker.domain.repository.TagRepository
import com.mknlabs.expensetracker.models.Tag
import com.mknlabs.expensetracker.models.TagStats
import com.mknlabs.expensetracker.utils.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

/**
 * Covers the bulk operations behind the Tag Management chips.
 *
 * Merge is the one that can lose data quietly: a target that is itself merged into another
 * tag would tombstone the tag the user chose to keep, and a source that is skipped leaves a
 * transaction pointing at a tag the user believed was gone. Delete is checked for the same
 * reason in bulk — a selection that is dropped on the floor looks exactly like a tag that
 * refused to die.
 */
@OptIn(ExperimentalCoroutinesApi::class)
class TagManagementViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `merging folds every other selected tag into the target`() = runTest {
        val repository = StubTagRepository(listOf(tag("a"), tag("b"), tag("c")))
        val viewModel = TagManagementViewModel(repository)

        viewModel.mergeTags(targetId = "a", sourceIds = listOf("a", "b", "c"))

        // The target is never a source of itself, so only b and c are folded in.
        assertEquals(listOf("b" to "a", "c" to "a"), repository.merges)
    }

    @Test
    fun `merging a selection that holds only the target does nothing`() = runTest {
        val repository = StubTagRepository(listOf(tag("a")))
        val viewModel = TagManagementViewModel(repository)

        viewModel.mergeTags(targetId = "a", sourceIds = listOf("a"))

        assertTrue(repository.merges.isEmpty())
    }

    @Test
    fun `deleting removes every selected tag`() = runTest {
        val repository = StubTagRepository(listOf(tag("a"), tag("b")))
        val viewModel = TagManagementViewModel(repository)

        viewModel.deleteTags(listOf("a", "b"))

        assertEquals(listOf("a", "b"), repository.deleted)
    }

    @Test
    fun `an empty delete selection is a no-op`() = runTest {
        val repository = StubTagRepository(listOf(tag("a")))
        val viewModel = TagManagementViewModel(repository)

        viewModel.deleteTags(emptyList())

        assertTrue(repository.deleted.isEmpty())
    }

    private fun tag(id: String) = Tag(id = id, name = id)
}

private class StubTagRepository(
    initial: List<Tag>
) : TagRepository {
    private val tags = MutableStateFlow(initial)

    val merges = mutableListOf<Pair<String, String>>()
    val deleted = mutableListOf<String>()

    override fun observeActiveTags(): Flow<List<Tag>> = tags
    override fun observeTagStats(): Flow<Map<String, TagStats>> = MutableStateFlow(emptyMap())
    override fun observeTagsForTransaction(transactionId: String): Flow<List<Tag>> = MutableStateFlow(emptyList())
    override fun observeAllTransactionTags(): Flow<Map<String, List<Tag>>> = MutableStateFlow(emptyMap())
    override suspend fun getTagIdsForTransaction(transactionId: String): List<String> = emptyList()

    override suspend fun createTag(name: String, colorHex: String?): Tag =
        Tag(id = name, name = name)

    override suspend fun renameTag(id: String, newName: String) = Unit
    override suspend fun updateTagColor(id: String, colorHex: String?) = Unit

    override suspend fun deleteTag(id: String) {
        deleted += id
    }

    override suspend fun mergeTags(sourceId: String, targetId: String) {
        merges += sourceId to targetId
    }

    override suspend fun setTransactionTags(transactionId: String, tagIds: List<String>) = Unit
}
