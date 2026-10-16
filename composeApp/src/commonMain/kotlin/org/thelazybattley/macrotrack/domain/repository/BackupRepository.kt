package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.BackupInfo
import org.thelazybattley.macrotrack.domain.model.LastBackup

interface BackupRepository {

    /** Everything on this phone as a backup file, ready to save. */
    suspend fun createBackup(): ByteArray

    /** Reads and checks a backup file. Throws BackupException when it can't be restored. */
    suspend fun readBackup(bytes: ByteArray): BackupInfo

    /** Replaces everything on this phone with a backup file's contents. Throws BackupException. */
    suspend fun restore(bytes: ByteArray)

    fun getLastBackup(): Flow<LastBackup?>

    /** Remembers that a backup of [sizeBytes] was just saved. */
    suspend fun recordBackup(sizeBytes: Int)
}
