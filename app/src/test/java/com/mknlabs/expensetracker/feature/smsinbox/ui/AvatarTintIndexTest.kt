package com.mknlabs.expensetracker.feature.smsinbox.ui

import com.mknlabs.expensetracker.ui.theme.AvatarTints
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The avatar tint is the only thing telling two senders apart in the inbox, and it is
 * computed rather than stored. A wrong answer here is either an out-of-bounds crash or a
 * row that changes colour as the list scrolls, so both ends are pinned below.
 */
class AvatarTintIndexTest {

    @Test
    fun `every name lands on a tint that exists`() {
        val names = listOf(
            "", "A", "Swiggy", "VM-HDFCBK", "Flipkart Hypg Yespay", "श्री", "a very long merchant name that goes on"
        )

        names.forEach { name ->
            val index = avatarTintIndex(name, AvatarTints.size)

            assertTrue("$index out of range for \"$name\"", index in AvatarTints.indices)
        }
    }

    @Test
    fun `the same sender always gets the same tint`() {
        // Stability is what makes the disc an identifier rather than decoration: a row
        // must not change colour when the list is re-projected or paged.
        repeat(5) {
            assertEquals(
                avatarTintIndex("Swiggy", AvatarTints.size),
                avatarTintIndex("Swiggy", AvatarTints.size)
            )
        }
    }

    @Test
    fun `a hash below zero still indexes a tint`() {
        // Kotlin's `%` keeps the dividend's sign, so this is where a naive implementation
        // would index at a negative position.
        val negativeHashName = generateSequence(0) { it + 1 }
            .map { "sender-$it" }
            .first { it.hashCode() < 0 }

        val index = avatarTintIndex(negativeHashName, AvatarTints.size)

        assertTrue("$index out of range", index in AvatarTints.indices)
    }

    @Test
    fun `different senders do not all collapse onto one tint`() {
        val senders = listOf("Swiggy", "VM-HDFCBK", "Flipkart", "Zomato", "Uber", "Amazon", "Dinesh Kumar Nayak")

        val tints = senders.map { avatarTintIndex(it, AvatarTints.size) }.toSet()

        assertTrue("every sender landed on the same tint", tints.size > 1)
    }

    @Test
    fun `an unusable palette size falls back to the first tint`() {
        assertEquals(0, avatarTintIndex("Swiggy", 0))
        assertEquals(0, avatarTintIndex("Swiggy", -4))
    }
}
