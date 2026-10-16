package org.thelazybattley.macrotrack.domain.model

/** A MacroTrack backup file that was read and checked, before it's restored. */
data class BackupInfo(
    /** When the backup was made, in milliseconds since 1970 (UTC). */
    val createdAtMillis: Long,
    /** The phone it was made on, e.g. "Pixel 8". */
    val device: String,
    val sizeBytes: Int,
    val foodLogCount: Int,
    val weightCount: Int
)

/** The last backup saved from this phone. */
data class LastBackup(
    val atMillis: Long,
    val sizeBytes: Int
)

/** Why a picked file can't be restored. */
enum class BackupProblem {
    /** Not a MacroTrack backup, or damaged. */
    NOT_A_BACKUP,

    /** Made by a newer version of the app, which this one can't read. */
    TOO_NEW
}

class BackupException(val problem: BackupProblem) : Exception(problem.name)
