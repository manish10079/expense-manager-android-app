package com.mkn0079.expensetracker.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mkn0079.expensetracker.ui.theme.BackgroundDark
import com.mkn0079.expensetracker.ui.theme.PurpleAccent
import com.mkn0079.expensetracker.ui.theme.PurplePrimary

@Composable
fun AboutScreen(
    onBackClick: () -> Unit,
    onPrepareForExternalActivity: () -> Unit = {}
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    val openUrl: (String) -> Unit = { url ->
        try {
            onPrepareForExternalActivity()
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    val sendEmail: (String) -> Unit = { email ->
        try {
            onPrepareForExternalActivity()
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:$email")
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            // Handle error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        AboutHeader(onBackClick = onBackClick)

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(24.dp))

            // App Icon
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(PurplePrimary.copy(alpha = 0.6f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Public, // Placeholder for app icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Expense Tracker",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "VERSION 1.0.0",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(top = 4.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // About App Card
            AboutInfoCard(
                icon = Icons.Filled.Info,
                title = "About App",
                description = "A simple app to manage your daily transactions with elegance. Designed for the modern user who values financial clarity and a premium interface experience. Every data point is treated as part of a high-end editorial vault."
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Developed By Card
            DeveloperCard(
                name = "Manish Kumar Nayak",
                email = "try.manish0079@gmail.com",
                onEmailClick = { sendEmail("try.manish0079@gmail.com") }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Support Section
            AboutSectionHeader(title = "SUPPORT")
            SupportLegalSection {
                AboutActionItem(
                    icon = Icons.Filled.Star,
                    title = "Rate App",
                    onClick = { /* Handle rate app */ }
                )
                AboutActionItem(
                    icon = Icons.Filled.Email,
                    title = "Send Feedback",
                    onClick = { sendEmail("try.manish0079@gmail.com") }
                )
                AboutActionItem(
                    icon = Icons.Filled.BugReport,
                    title = "Report Bug",
                    onClick = { /* Handle report bug */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legal Section
            AboutSectionHeader(title = "LEGAL")
            SupportLegalSection {
                AboutActionItem(
                    icon = Icons.Filled.Lock,
                    title = "Privacy Policy",
                    onClick = { openUrl("https://example.com/privacy") }
                )
                AboutActionItem(
                    icon = Icons.Filled.Description,
                    title = "Terms & Conditions",
                    onClick = { openUrl("https://example.com/terms") }
                )
                AboutActionItem(
                    icon = Icons.Filled.ListAlt,
                    title = "Open Source Licenses",
                    onClick = { /* Show licenses */ }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Follow Us Section
            AboutSectionHeader(title = "FOLLOW US")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                SocialButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Public,
                    label = "WEBSITE",
                    onClick = { openUrl("https://example.com") }
                )
                SocialButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Code,
                    label = "GITHUB",
                    onClick = { openUrl("https://github.com/manish10079") }
                )
                SocialButton(
                    modifier = Modifier.weight(1f),
                    icon = Icons.Filled.Work,
                    label = "LINKEDIN",
                    onClick = { openUrl("https://linkedin.com/in/manishkumar10079") }
                )
            }

            Spacer(modifier = Modifier.height(48.dp))

            Text(
                text = "HANDCRAFTED WITH PRECISION AND LOVE",
                color = Color.White.copy(alpha = 0.3f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun AboutHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackClick) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White
            )
        }
        Text(
            text = "About",
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(start = 8.dp)
        )
    }
}

@Composable
private fun AboutInfoCard(
    icon: ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .padding(24.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = PurpleAccent,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = description,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 15.sp,
            lineHeight = 22.sp
        )
    }
}

@Composable
private fun DeveloperCard(
    name: String,
    email: String,
    onEmailClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onEmailClick)
            .padding(24.dp)
    ) {
        Column {
            Text(
                text = "DEVELOPED BY",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = email,
                color = PurpleAccent.copy(alpha = 0.8f),
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun AboutSectionHeader(title: String) {
    Text(
        text = title,
        color = Color.White.copy(alpha = 0.4f),
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    )
}

@Composable
private fun SupportLegalSection(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
    ) {
        content()
    }
}

@Composable
private fun AboutActionItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PurpleAccent.copy(alpha = 0.8f),
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            color = Color.White.copy(alpha = 0.9f),
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.2f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun SocialButton(
    modifier: Modifier = Modifier,
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White.copy(alpha = 0.08f))
            .clickable(onClick = onClick)
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.9f),
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
