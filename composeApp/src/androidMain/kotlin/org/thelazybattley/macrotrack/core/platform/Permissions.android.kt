package org.thelazybattley.macrotrack.core.platform

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LifecycleResumeEffect

internal fun Context.hasPermission(permission: String): Boolean =
    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

@Composable
actual fun rememberNotificationPermissionState(): PermissionState =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        rememberPermissionState(permission = Manifest.permission.POST_NOTIFICATIONS)
    } else {
        remember { PermissionState(isGranted = true, isPermanentlyDenied = false, request = {}) }
    }

@Composable
private fun rememberPermissionState(permission: String): PermissionState {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var isGranted by remember { mutableStateOf(value = context.hasPermission(permission = permission)) }
    var isPermanentlyDenied by remember { mutableStateOf(value = false) }
    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) { granted ->
        isGranted = granted
        // Once someone denies twice Android stops showing the dialog, so the only way back is the app's settings.
        isPermanentlyDenied = !isGranted && activity != null && !activity.shouldShowRationale(permission = permission)
    }
    // Picks up access granted in settings while the app was in the background.
    LifecycleResumeEffect(key1 = Unit) {
        isGranted = context.hasPermission(permission = permission)
        onPauseOrDispose {}
    }
    return PermissionState(
        isGranted = isGranted,
        isPermanentlyDenied = isPermanentlyDenied,
        request = {
            if (isPermanentlyDenied) {
                context.startActivity(
                    Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.fromParts("package", context.packageName, null))
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
            } else {
                launcher.launch(permission)
            }
        }
    )
}

private fun Activity.shouldShowRationale(permission: String): Boolean =
    ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
