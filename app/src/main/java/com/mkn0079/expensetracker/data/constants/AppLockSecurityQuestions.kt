package com.mkn0079.expensetracker.data.constants

data class AppLockSecurityQuestion(
    val id: String,
    val prompt: String
)

val appLockSecurityQuestions = listOf(
    AppLockSecurityQuestion(
        id = "first_childhood_crush",
        prompt = "What was your first childhood crush name?"
    ),
    AppLockSecurityQuestion(
        id = "first_school",
        prompt = "What was the name of your first school?"
    ),
    AppLockSecurityQuestion(
        id = "childhood_friend",
        prompt = "What is the first name of your childhood best friend?"
    ),
    AppLockSecurityQuestion(
        id = "birth_city",
        prompt = "In which city were you born?"
    ),
    AppLockSecurityQuestion(
        id = "favorite_teacher",
        prompt = "What was the last name of your favorite teacher?"
    ),
    AppLockSecurityQuestion(
        id = "pet_name",
        prompt = "What was the name of your first pet?"
    )
)

fun getAppLockSecurityQuestionPrompt(questionId: String?): String? {
    return appLockSecurityQuestions.firstOrNull { it.id == questionId }?.prompt
}
