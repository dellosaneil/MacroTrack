package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class RestoreBackupUseCase(private val repository: BackupRepository) {

    suspend operator fun invoke(bytes: ByteArray) = repository.restore(bytes = bytes)
}
