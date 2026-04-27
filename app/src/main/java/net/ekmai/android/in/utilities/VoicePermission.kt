package net.ekmai.android.`in`.utilities

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*

@Composable
fun rememberMicPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit = {}
): () -> Unit {
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onGranted() else onDenied()
    }
    return { launcher.launch(Manifest.permission.RECORD_AUDIO) }
}