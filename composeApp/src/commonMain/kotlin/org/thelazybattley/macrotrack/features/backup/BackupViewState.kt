package org.thelazybattley.macrotrack.features.backup

import org.thelazybattley.macrotrack.core.platform.SharedFile
import org.thelazybattley.macrotrack.domain.model.BackupInfo
import org.thelazybattley.macrotrack.domain.model.BackupProblem
import org.thelazybattley.macrotrack.domain.model.LastBackup
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.features.backup.export.ExportData
import org.thelazybattley.macrotrack.features.backup.export.ExportFile
import org.thelazybattley.macrotrack.features.backup.export.ExportFormat
import org.thelazybattley.macrotrack.features.backup.export.ExportItem
import org.thelazybattley.macrotrack.features.backup.export.ExportRange

/** Why a file is being saved, so the result goes to the right place. */
enum class SaveReason { BACKUP, BEFORE_RESTORE, EXPORT }

/** Asks the screen to open the save dialog for [file]; the screen reports back through onSaveFinished. */
data class SaveRequest(val file: SharedFile, val reason: SaveReason)

sealed interface ExportSheet {
    data class Preparing(val item: ExportItem?) : ExportSheet
    data class Ready(val file: ExportFile, val isSaved: Boolean = false) : ExportSheet
}

sealed interface RestoreDialog {
    /** A picked backup, waiting for the user to confirm it replaces what's here. */
    data class Confirm(val info: BackupInfo, val bytes: ByteArray, val backUpFirst: Boolean = true) : RestoreDialog

    data class Working(val info: BackupInfo, val isBackingUp: Boolean) : RestoreDialog

    data class Done(val info: BackupInfo, val savedFirst: Boolean) : RestoreDialog

    data class Failed(val problem: BackupProblem) : RestoreDialog
}

data class BackupViewState(
    val data: ExportData = ExportData(),
    val unitSystem: UnitSystem = UnitSystem.METRIC,
    val lastBackup: LastBackup? = null,
    /** A backup is being packed or the save dialog for it is open. */
    val isBackingUp: Boolean = false,
    /** The save dialog for a backup is open. */
    val isChoosingPlace: Boolean = false,
    val selectedItems: Set<ExportItem> = ExportItem.entries.toSet(),
    val range: ExportRange = ExportRange.ALL_TIME,
    val format: ExportFormat = ExportFormat.CSV,
    val exportSheet: ExportSheet? = null,
    val restoreDialog: RestoreDialog? = null,
    val saveRequest: SaveRequest? = null,
    val isPickRequested: Boolean = false,
    /** The restore finished and the user chose to go to Today. */
    val goToToday: Boolean = false
) {
    val canExport: Boolean get() = selectedItems.isNotEmpty()

    val isAllSelected: Boolean get() = selectedItems.size == ExportItem.entries.size

    val recipeCount: Int get() = data.recipes.size

    val foodCount: Int get() = data.foods.size
}
