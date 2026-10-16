package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class RecordBackupUseCase(private val repository: BackupRepository) {

    suspend operator fun invoke(sizeBytes: Int) = repository.recordBackup(sizeBytes = sizeBytes)
}
