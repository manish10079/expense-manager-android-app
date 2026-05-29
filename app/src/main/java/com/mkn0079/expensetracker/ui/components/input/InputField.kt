package com.mkn0079.expensetracker.ui.components.input

import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import com.mkn0079.expensetracker.R
import com.mkn0079.expensetracker.ui.theme.ExpenseTrackerTheme

@Composable
fun InputFieldCard(
    title: String,
    value: String,
    onValueChange: (String) -> Unit,
    inputType: InputType,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    placeholder: String? = null,
    isEnabled: Boolean = true,
    isError: Boolean = false,
    errorText: String? = null,
    leadingIcon: ImageVector? = null,
    onClick: (() -> Unit)? = null,
    trailingContent: (@Composable (() -> Unit))? = null
) {
    val colorScheme = MaterialTheme.colorScheme

    val containerColor = colorScheme.surface
    val primary = colorScheme.primary
    val onSurface = colorScheme.onSurface
    val onSurfaceVariant = colorScheme.onSurfaceVariant
    val errorColor = colorScheme.error
    
    val containerShape = RoundedCornerShape(28.dp)
    val borderColor = colorScheme.outlineVariant.copy(
        alpha = if (isEnabled) 0.4f else 0.2f
    )

    val isClickable = inputType == InputType.Date && isEnabled

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isClickable && onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = containerShape,
        color = containerColor,
        border = BorderStroke(width = 1.dp, color = borderColor),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (leadingIcon != null) {
                Column(
                    modifier = Modifier.padding(end = 12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = leadingIcon,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(4.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        when (inputType) {
                            InputType.Text -> {
                                BasicTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    enabled = isEnabled,
                                    textStyle = LocalTextStyle.current.copy(
                                        color = onSurface,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (value.isEmpty() && placeholder != null) {
                                            Text(
                                                text = placeholder,
                                                color = onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            InputType.Email -> {
                                val isValid = remember(value) {
                                    android.util.Patterns.EMAIL_ADDRESS.matcher(value).matches()
                                }

                                Column {
                                    BasicTextField(
                                        value = value,
                                        onValueChange = onValueChange,
                                        enabled = isEnabled,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Email
                                        ),
                                        textStyle = LocalTextStyle.current.copy(color = onSurface, fontSize = 16.sp),
                                        cursorBrush = SolidColor(primary),
                                        modifier = Modifier.fillMaxWidth(),
                                        decorationBox = { innerTextField ->
                                            if (value.isEmpty() && placeholder != null) {
                                                Text(placeholder, color = onSurfaceVariant.copy(alpha = 0.6f))
                                            }
                                            innerTextField()
                                        }
                                    )

                                    if (value.isNotEmpty() && !isValid) {
                                        Text(
                                            text = stringResource(R.string.error_invalid_email),
                                            color = errorColor,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            InputType.Phone -> {
                                BasicTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Phone
                                    ),
                                    textStyle = LocalTextStyle.current.copy(color = onSurface, fontSize = 16.sp),
                                    cursorBrush = SolidColor(primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (value.isEmpty() && placeholder != null) {
                                            Text(
                                                text = placeholder,
                                                color = onSurfaceVariant.copy(alpha = 0.5f),
                                                fontSize = 16.sp
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }

                            InputType.Date -> {
                                Text(
                                    text = if (value.isEmpty()) placeholder ?: stringResource(R.string.title_select_date) else value,
                                    color = if (value.isEmpty()) onSurfaceVariant.copy(alpha = 0.6f) else onSurface,
                                    fontSize = 16.sp
                                )
                            }

                            InputType.Password -> {
                                BasicTextField(
                                    value = value,
                                    onValueChange = onValueChange,
                                    enabled = isEnabled,
                                    keyboardOptions = KeyboardOptions(
                                        keyboardType = KeyboardType.Password
                                    ),
                                    visualTransformation = PasswordVisualTransformation(),
                                    textStyle = LocalTextStyle.current.copy(
                                        color = onSurface,
                                        fontSize = 16.sp
                                    ),
                                    cursorBrush = SolidColor(primary),
                                    modifier = Modifier.fillMaxWidth(),
                                    decorationBox = { innerTextField ->
                                        if (value.isEmpty() && placeholder != null) {
                                            Text(
                                                text = placeholder,
                                                color = onSurfaceVariant.copy(alpha = 0.6f)
                                            )
                                        }
                                        innerTextField()
                                    }
                                )
                            }
                        }
                    }
                }

                if (!subtitle.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = onSurfaceVariant
                    )
                }

                if (isError && !errorText.isNullOrEmpty()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = errorText,
                        color = errorColor,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            if (trailingContent != null) {
                Spacer(Modifier.width(8.dp))
                trailingContent()
            }
        }
    }
}

@Preview(name = "Light Mode", showBackground = true)
@Preview(name = "Dark Mode", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewInputFieldCard() {
    ExpenseTrackerTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                InputFieldCard(
                    title = "Full Name",
                    value = "John Doe",
                    onValueChange = {},
                    inputType = InputType.Text,
                    leadingIcon = Icons.Rounded.Person,
                    placeholder = "Enter your name"
                )

                InputFieldCard(
                    title = "Email Address",
                    value = "",
                    onValueChange = {},
                    inputType = InputType.Email,
                    leadingIcon = Icons.Rounded.Email,
                    placeholder = "example@mail.com"
                )

                InputFieldCard(
                    title = "Phone Number",
                    value = "9876543210",
                    onValueChange = {},
                    inputType = InputType.Phone,
                    leadingIcon = Icons.Rounded.Phone
                )

                InputFieldCard(
                    title = "Birth Date",
                    value = "01 Jan 2000",
                    onValueChange = {},
                    inputType = InputType.Date,
                    leadingIcon = Icons.Rounded.CalendarToday,
                    onClick = {}
                )
            }
        }
    }
}
