package com.mknlabs.expensetracker.ai

import android.content.Intent
import android.net.Uri
import android.os.Build

data class IncomingShare(
    val documentUri: Uri? = null,
    val text: String? = null,
)

fun Intent.toIncomingShare(): IncomingShare? {
    val action = action ?: return null
    if (action != Intent.ACTION_SEND && action != Intent.ACTION_SEND_MULTIPLE) return null
    val text = getStringExtra(Intent.EXTRA_TEXT)?.trim().orEmpty().ifBlank { null }
    val uri = when (action) {
        Intent.ACTION_SEND -> extraStreamUri()
        Intent.ACTION_SEND_MULTIPLE -> extraStreamUris().firstOrNull()
        else -> null
    }
    if (uri == null && text == null) return null
    return IncomingShare(documentUri = uri, text = text)
}

private fun Intent.extraStreamUri(): Uri? {
    val extra = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableExtra(Intent.EXTRA_STREAM)
    }
    if (extra != null) return extra
    val clip = clipData ?: return null
    if (clip.itemCount < 1) return null
    return clip.getItemAt(0).uri
}

private fun Intent.extraStreamUris(): List<Uri> {
    val list = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        getParcelableArrayListExtra(Intent.EXTRA_STREAM, Uri::class.java)
    } else {
        @Suppress("DEPRECATION")
        getParcelableArrayListExtra(Intent.EXTRA_STREAM)
    }
    return list.orEmpty()
}
