package com.mknlabs.expensetracker.feature.settings.ui

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.navigation.compose.hiltViewModel
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.domain.repository.ReferralError

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReferAFriendScreen(
    onBackClick: () -> Unit,
    onLinkAccountClick: () -> Unit,
    viewModel: ReferAFriendViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val playUrl = stringResource(R.string.url_rate_app)
    var enterCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.title_refer_a_friend)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (state.isAnonymous) {
                Text(
                    text = stringResource(R.string.msg_refer_register_for_code),
                    style = MaterialTheme.typography.bodyLarge
                )
                Button(onClick = onLinkAccountClick, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.btn_register_for_referral))
                }
            } else {
                Text(
                    text = stringResource(R.string.msg_refer_how_it_works),
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = state.code,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(stringResource(R.string.label_refer_rewards_this_month, state.rewardsThisMonth, 4))
                TextButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(state.code))
                        Toast.makeText(context, context.getString(R.string.msg_code_copied), Toast.LENGTH_SHORT).show()
                    }
                ) {
                    Text(stringResource(R.string.btn_copy_code))
                }
                Button(
                    onClick = {
                        val bodies = listOf(
                            context.getString(R.string.msg_share_app_chat, playUrl),
                            context.getString(R.string.msg_share_app_features, playUrl),
                            context.getString(R.string.msg_share_app_punchy, playUrl)
                        )
                        val text = bodies.random() + "\n\n" + context.getString(R.string.msg_use_my_code, state.code)
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, text)
                        }
                        context.startActivity(Intent.createChooser(send, context.getString(R.string.title_refer_a_friend)))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(stringResource(R.string.btn_share_code))
                }
                if (!state.alreadyReferred) {
                    OutlinedTextField(
                        value = enterCode,
                        onValueChange = { enterCode = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(stringResource(R.string.label_enter_friend_code)) },
                        singleLine = true
                    )
                    Button(
                        onClick = { viewModel.applyCode(enterCode) },
                        enabled = enterCode.isNotBlank() && !state.applyBusy,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.btn_apply_code))
                    }
                    state.applyError?.let { err ->
                        Text(
                            text = stringResource(referralErrorRes(err)),
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    state.applyWelcomeDays?.let { days ->
                        if (days > 0) {
                            Text(stringResource(R.string.msg_welcome_days, days))
                        }
                    }
                }
            }
        }
    }
}

private fun referralErrorRes(error: ReferralError): Int = when (error) {
    ReferralError.NotSignedIn -> R.string.error_refer_not_signed_in
    ReferralError.InvalidCode -> R.string.error_refer_invalid
    ReferralError.OwnCode -> R.string.error_refer_own_code
    ReferralError.AlreadyReferred -> R.string.error_refer_already
    ReferralError.Network -> R.string.error_refer_network
    ReferralError.Unknown -> R.string.error_refer_unknown
}
