package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class ReadBackupUseCase(private val repository: BackupRepository) {

    suspend operator fun invoke(bytes: ByteArray) = repository.readBackup(bytes = bytes)
}
