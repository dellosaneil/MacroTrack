package org.thelazybattley.macrotrack.core.platform

import androidx.compose.runtime.Composable

/** A file made in the app, to save or share. */
class SharedFile(val name: String, val mimeType: String, val bytes: ByteArray)

/** The system's save dialog, share sheet and file picker. */
interface FileActions {

    /** Lets the user pick where to save [file]; [onResult] gets true once it's written, false if they cancel. */
    fun save(file: SharedFile, onResult: (Boolean) -> Unit)

    /** Opens the share sheet with [file]. */
    fun share(file: SharedFile)

    /** Lets the user pick a file; [onPicked] gets its contents, or null if they cancel or it can't be read. */
    fun pick(onPicked: (ByteArray?) -> Unit)
}

@Composable
expect fun rememberFileActions(): FileActions
