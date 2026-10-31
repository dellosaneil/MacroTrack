package org.thelazybattley.macrotrack.core.platform

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

@Composable
actual fun rememberFileActions(): FileActions {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val actions = remember { AndroidFileActions(context = context.applicationContext, scope = scope) }
    actions.saveLauncher = rememberLauncherForActivityResult(contract = CreateDocument()) { uri -> actions.onSaveTarget(uri = uri) }
    actions.pickLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.OpenDocument()) { uri ->
        actions.onPicked(uri = uri)
    }
    actions.activityContext = context
    return actions
}

private class AndroidFileActions(
    private val context: Context,
    private val scope: CoroutineScope
) : FileActions {
    lateinit var saveLauncher: androidx.activity.result.ActivityResultLauncher<Pair<String, String>>
    lateinit var pickLauncher: androidx.activity.result.ActivityResultLauncher<Array<String>>
    var activityContext: Context = context

    private var pendingSave: Pair<SharedFile, (Boolean) -> Unit>? = null
    private var pendingPick: ((ByteArray?) -> Unit)? = null

    override fun save(file: SharedFile, onResult: (Boolean) -> Unit) {
        pendingSave = file to onResult
        saveLauncher.launch(input = file.name to file.mimeType)
    }

    fun onSaveTarget(uri: Uri?) {
        val (file, onResult) = pendingSave ?: return
        pendingSave = null
        if (uri == null) {
            onResult(false)
            return
        }
        scope.launch {
            val isWritten = withContext(context = Dispatchers.IO) {
                runCatching {
                    context.contentResolver.openOutputStream(uri, "wt")?.use { it.write(file.bytes) } != null
                }.getOrDefault(defaultValue = false)
            }
            onResult(isWritten)
        }
    }

    override fun saveImage(file: SharedFile, onResult: (Boolean) -> Unit) {
        // Before Android 10, writing to the shared Pictures folder needs the storage permission.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return save(file = file, onResult = onResult)
        scope.launch {
            val isSaved = withContext(context = Dispatchers.IO) {
                runCatching {
                    val resolver = context.contentResolver
                    val details = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, file.name)
                        put(MediaStore.Images.Media.MIME_TYPE, file.mimeType)
                        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + PICTURES_FOLDER)
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }
                    val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, details) ?: return@runCatching false
                    resolver.openOutputStream(uri)?.use { it.write(file.bytes) }
                    details.clear()
                    details.put(MediaStore.Images.Media.IS_PENDING, 0)
                    resolver.update(uri, details, null, null)
                    true
                }.getOrDefault(defaultValue = false)
            }
            onResult(isSaved)
        }
    }

    override fun share(file: SharedFile) {
        scope.launch {
            val uri = withContext(context = Dispatchers.IO) {
                val folder = File(context.cacheDir, SHARE_FOLDER).apply { mkdirs() }
                val target = File(folder, file.name).apply { writeBytes(file.bytes) }
                FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", target)
            }
            val send = Intent(Intent.ACTION_SEND).apply {
                type = file.mimeType
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            val chooser = Intent.createChooser(send, null)
            if (activityContext !is Activity) chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            activityContext.startActivity(chooser)
        }
    }

    override fun pick(onPicked: (ByteArray?) -> Unit) {
        pendingPick = onPicked
        // Backups are JSON, but file managers label .json files inconsistently, so any file can be picked and checked.
        pickLauncher.launch(input = arrayOf("*/*"))
    }

    fun onPicked(uri: Uri?) {
        val onPicked = pendingPick ?: return
        pendingPick = null
        if (uri == null) {
            onPicked(null)
            return
        }
        scope.launch {
            val bytes = withContext(context = Dispatchers.IO) {
                runCatching { context.contentResolver.openInputStream(uri)?.use { it.readBytes() } }.getOrNull()
            }
            onPicked(bytes)
        }
    }

    companion object {
        /** Matches the cache-path in res/xml/file_paths.xml. */
        const val SHARE_FOLDER = "shared"

        /** Saved images go in Pictures/FitMetric. */
        const val PICTURES_FOLDER = "FitMetric"
    }
}

/** The system "save as" dialog for a given file name and type. */
private class CreateDocument : ActivityResultContract<Pair<String, String>, Uri?>() {
    override fun createIntent(context: Context, input: Pair<String, String>): Intent =
        Intent(Intent.ACTION_CREATE_DOCUMENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(input.second)
            .putExtra(Intent.EXTRA_TITLE, input.first)

    override fun parseResult(resultCode: Int, intent: Intent?): Uri? =
        intent?.data?.takeIf { resultCode == Activity.RESULT_OK }
}
