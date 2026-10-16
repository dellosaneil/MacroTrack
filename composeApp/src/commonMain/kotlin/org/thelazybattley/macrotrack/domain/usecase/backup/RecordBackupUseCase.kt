package org.thelazybattley.macrotrack.domain.usecase.backup

import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.repository.BackupRepository

class RecordBackupUseCase(private val repository: BackupRepository, private val telemetry: Telemetry) {

    suspend operator fun invoke(sizeBytes: Int) {
        repository.recordBackup(sizeBytes = sizeBytes)
        telemetry.logEvent(name = TelemetryEvent.BACKUP_CREATED)
    }
}
