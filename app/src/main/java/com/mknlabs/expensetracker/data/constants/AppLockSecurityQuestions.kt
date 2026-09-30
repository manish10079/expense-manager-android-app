package com.mknlabs.expensetracker.data.constants
 
import com.mknlabs.expensetracker.R
import kotlin.random.Random

data class AppLockSecurityQuestion(
    val id: String,
    val promptResId: Int
)

/**
 * Number of questions presented during PIN recovery: the user's real saved
 * question plus (this count - 1) randomly chosen decoys.
 */
const val APP_LOCK_RECOVERY_QUESTION_COUNT = 3

val appLockSecurityQuestions = listOf(
    AppLockSecurityQuestion(
        id = "first_childhood_crush",
        promptResId = R.string.question_childhood_crush
    ),
    AppLockSecurityQuestion(
        id = "first_school",
        promptResId = R.string.question_first_school
    ),
    AppLockSecurityQuestion(
        id = "childhood_friend",
        promptResId = R.string.question_childhood_friend
    ),
    AppLockSecurityQuestion(
        id = "birth_city",
        promptResId = R.string.question_birth_city
    ),
    AppLockSecurityQuestion(
        id = "favorite_teacher",
        promptResId = R.string.question_favorite_teacher
    ),
    AppLockSecurityQuestion(
        id = "pet_name",
        promptResId = R.string.question_pet_name
    )
)

/**
 * Builds the shuffled set of questions shown during PIN recovery: the user's
 * saved question plus randomly selected decoys from the pool. Returns an empty
 * list when the saved question is unknown (recovery is unavailable). The
 * [random] parameter is injectable so tests can make the selection deterministic.
 *
 * The real question is always included and decoys are drawn from the remaining
 * pool, so the presentation never leaks which entry is the saved one by position
 * (the caller iterates the returned list as-is).
 */
fun buildRecoveryQuestionOptions(
    savedQuestionId: String?,
    random: Random = Random.Default,
    optionCount: Int = APP_LOCK_RECOVERY_QUESTION_COUNT
): List<AppLockSecurityQuestion> {
    val savedQuestion = appLockSecurityQuestions.firstOrNull { it.id == savedQuestionId }
        ?: return emptyList()
    if (optionCount <= 1) {
        return listOf(savedQuestion)
    }

    val decoys = appLockSecurityQuestions
        .filter { it.id != savedQuestion.id }
        .shuffled(random)
        .take(optionCount - 1)

    return (decoys + savedQuestion).shuffled(random)
}

