package org.thelazybattley.macrotrack.features.backup

import org.thelazybattley.macrotrack.features.backup.export.ExportFormat
import org.thelazybattley.macrotrack.features.backup.export.ExportItem
import org.thelazybattley.macrotrack.features.backup.export.ExportRange

interface BackupCallbacks {

    fun onBackUpNow()

    /** The screen opened the save dialog for the pending SaveRequest. */
    fun onSaveStarted()

    fun onSaveFinished(reason: SaveReason, isSaved: Boolean, sizeBytes: Int)

    fun onRestoreClick()

    /** The screen opened the file picker. */
    fun onPickStarted()

    fun onBackupPicked(bytes: ByteArray?)

    fun onBackUpFirstToggled()

    fun onConfirmRestore()

    fun onCloseRestore()

    fun onGoToToday()

    fun onItemToggled(item: ExportItem)

    fun onToggleAll()

    fun onRangeSelected(range: ExportRange)

    fun onFormatSelected(format: ExportFormat)

    fun onExport()

    fun onSaveExport()

    fun onCloseExport()

    companion object {
        fun default() = object : BackupCallbacks {
            override fun onBackUpNow() {}
            override fun onSaveStarted() {}
            override fun onSaveFinished(reason: SaveReason, isSaved: Boolean, sizeBytes: Int) {}
            override fun onRestoreClick() {}
            override fun onPickStarted() {}
            override fun onBackupPicked(bytes: ByteArray?) {}
            override fun onBackUpFirstToggled() {}
            override fun onConfirmRestore() {}
            override fun onCloseRestore() {}
            override fun onGoToToday() {}
            override fun onItemToggled(item: ExportItem) {}
            override fun onToggleAll() {}
            override fun onRangeSelected(range: ExportRange) {}
            override fun onFormatSelected(format: ExportFormat) {}
            override fun onExport() {}
            override fun onSaveExport() {}
            override fun onCloseExport() {}
        }
    }
}
