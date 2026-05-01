package com.mkn0079.expensetracker.ui.components.input

sealed class InputType {
    object Text : InputType()
    object Email : InputType()
    object Phone : InputType()
    object Date : InputType()
}

