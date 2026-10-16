package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class GetLastBackupUseCase(private val repository: BackupRepository) {

    operator fun invoke() = repository.getLastBackup()
}
