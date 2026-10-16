package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class CreateBackupUseCase(private val repository: BackupRepository) {

    suspend operator fun invoke(): ByteArray = repository.createBackup()
}
