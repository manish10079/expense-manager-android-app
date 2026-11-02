package com.mknlabs.expensetracker.core.ui.components

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.adamglin.PhosphorIcons
import com.adamglin.phosphoricons.Regular
import com.adamglin.phosphoricons.regular.Camera
import com.adamglin.phosphoricons.regular.CaretRight
import com.adamglin.phosphoricons.regular.Check
import com.adamglin.phosphoricons.regular.File
import com.adamglin.phosphoricons.regular.PaperPlaneTilt
import com.mknlabs.expensetracker.R
import com.mknlabs.expensetracker.core.ui.theme.cta
import com.mknlabs.expensetracker.core.ui.theme.genderRow
import com.mknlabs.expensetracker.core.ui.theme.isDark
import com.mknlabs.expensetracker.core.ui.theme.onCta
import com.mknlabs.expensetracker.core.ui.theme.profileSheet
import com.mknlabs.expensetracker.core.ui.theme.sheet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ReceiptAttachmentSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    onPickFile: () -> Unit,
    onCapturePhoto: () -> Unit,
    onSendUris: (List<Uri>) -> Unit,
) {
    val scheme = MaterialTheme.colorScheme
    val dark = scheme.isDark
    val sheetColor = if (dark) scheme.profileSheet else scheme.sheet
    val itemBg = if (dark) scheme.genderRow else Color(0xFFF1F5F9)
    val subtext = if (dark) Color(0xFF7F91A4) else scheme.onSurfaceVariant
    val context = LocalContext.current
    val permissions = remember {
        buildList {
            if (Build.VERSION.SDK_INT >= 33) {
                add(Manifest.permission.READ_MEDIA_IMAGES)
                if (Build.VERSION.SDK_INT >= 34) {
                    add(Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED)
                }
            } else {
                add(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }
    var granted by remember { mutableStateOf(false) }
    var gallery by remember { mutableStateOf<List<Uri>>(emptyList()) }
    val selected = remember { mutableStateListOf<Uri>() }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { result ->
        granted = result.values.any { it }
    }

    fun hasGalleryAccess(): Boolean {
        return permissions.any { perm ->
            ContextCompat.checkSelfPermission(context, perm) == PackageManager.PERMISSION_GRANTED
        }
    }

    LaunchedEffect(visible) {
        if (!visible) {
            selected.clear()
            return@LaunchedEffect
        }
        granted = hasGalleryAccess()
        if (!granted) permissionLauncher.launch(permissions)
    }
    LaunchedEffect(visible, granted) {
        if (!visible) {
            gallery = emptyList()
            return@LaunchedEffect
        }
        gallery = withContext(Dispatchers.IO) { loadRecentImages(context) }
    }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(180)),
        exit = fadeOut(tween(140)),
    ) {
        Box(Modifier.fillMaxSize()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onDismiss,
                    ),
            )
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(0.65f)
                    .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
                    .background(sheetColor)
                    .windowInsetsPadding(WindowInsets.navigationBars.union(WindowInsets.ime)),
            ) {
                Box(
                    Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 10.dp, bottom = 8.dp)
                        .width(40.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(if (dark) Color(0x9964758B) else Color(0xFFCBD5E1)),
                )
                Box(Modifier.weight(1f)) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3),
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 12.dp, end = 12.dp, bottom = if (selected.isEmpty()) 16.dp else 80.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(itemBg)
                                    .border(
                                        1.dp,
                                        if (dark) Color(0x9964758B) else Color(0xFFE2E8F0),
                                        RoundedCornerShape(12.dp),
                                    )
                                    .clickable(onClick = onPickFile)
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Box(
                                    Modifier
                                        .size(32.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0x3360A5FA)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        PhosphorIcons.Regular.File,
                                        contentDescription = null,
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(16.dp),
                                    )
                                }
                                Column(
                                    Modifier
                                        .weight(1f)
                                        .padding(horizontal = 10.dp),
                                    verticalArrangement = Arrangement.spacedBy(0.dp),
                                ) {
                                    Text(
                                        stringResource(R.string.label_attach_file),
                                        color = scheme.onSurface,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp,
                                        lineHeight = 16.sp,
                                    )
                                    Text(
                                        stringResource(R.string.desc_attach_file),
                                        color = subtext,
                                        fontSize = 12.sp,
                                        lineHeight = 14.sp,
                                    )
                                }
                                Icon(
                                    PhosphorIcons.Regular.CaretRight,
                                    contentDescription = null,
                                    tint = subtext,
                                    modifier = Modifier.size(16.dp),
                                )
                            }
                        }
                        item(span = { GridItemSpan(3) }) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp, bottom = 4.dp, start = 4.dp, end = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Text(
                                    stringResource(R.string.label_gallery),
                                    color = subtext,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                )
                                Text(
                                    stringResource(R.string.label_all_media),
                                    modifier = Modifier.clickable {
                                        granted = hasGalleryAccess()
                                        if (!granted) {
                                            permissionLauncher.launch(permissions)
                                        }
                                    },
                                    color = scheme.cta,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 12.sp,
                                )
                            }
                        }
                        item {
                            Column(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (dark) Color(0xFF1E293B) else Color(0xFFE2E8F0))
                                    .border(
                                        1.dp,
                                        if (dark) Color(0xFF334155) else Color(0xFFCBD5E1),
                                        RoundedCornerShape(8.dp),
                                    )
                                    .clickable(onClick = onCapturePhoto),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                            ) {
                                Box(
                                    Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(scheme.cta.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Icon(
                                        PhosphorIcons.Regular.Camera,
                                        contentDescription = null,
                                        tint = scheme.cta,
                                        modifier = Modifier.size(20.dp),
                                    )
                                }
                                Text(
                                    stringResource(R.string.label_camera),
                                    modifier = Modifier.padding(top = 4.dp),
                                    color = if (dark) Color(0xFFCBD5E1) else scheme.onSurface,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                )
                            }
                        }
                        items(gallery, key = { it.toString() }) { uri ->
                            val isSelected = uri in selected
                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        if (isSelected) {
                                            selected.clear()
                                        } else {
                                            selected.clear()
                                            selected.add(uri)
                                        }
                                    },
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(uri)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) scheme.cta else Color.Black.copy(alpha = 0.3f))
                                        .border(2.dp, Color.White.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            PhosphorIcons.Regular.Check,
                                            contentDescription = null,
                                            tint = scheme.onCta,
                                            modifier = Modifier.size(12.dp),
                                        )
                                    }
                                }
                            }
                        }
                    }
                    if (selected.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .fillMaxWidth()
                                .background(if (dark) scheme.genderRow else Color(0xFFF8FAFC))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                pluralStringResource(
                                    R.plurals.label_photos_selected,
                                    selected.size,
                                    selected.size,
                                ),
                                color = scheme.onSurface,
                                fontWeight = FontWeight.Medium,
                                fontSize = 14.sp,
                            )
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(scheme.cta)
                                    .clickable { onSendUris(selected.toList()) }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                            ) {
                                Text(
                                    stringResource(R.string.label_send),
                                    color = scheme.onCta,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                )
                                Icon(
                                    PhosphorIcons.Regular.PaperPlaneTilt,
                                    contentDescription = null,
                                    tint = scheme.onCta,
                                    modifier = Modifier.size(14.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun loadRecentImages(context: Context): List<Uri> {
    val uris = mutableListOf<Uri>()
    val collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
    val projection = arrayOf(MediaStore.Images.Media._ID)
    val sort = "${MediaStore.Images.Media.DATE_ADDED} DESC"
    context.contentResolver.query(collection, projection, null, null, sort)?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
        while (cursor.moveToNext() && uris.size < 80) {
            val id = cursor.getLong(idCol)
            uris += ContentUris.withAppendedId(collection, id)
        }
    }
    return uris
}
