package com.mkn0079.expensetracker.data.constants
 
import com.mkn0079.expensetracker.R

data class AppLockSecurityQuestion(
    val id: String,
    val promptResId: Int
)

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

