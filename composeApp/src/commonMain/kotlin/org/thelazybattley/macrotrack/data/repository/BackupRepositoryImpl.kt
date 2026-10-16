package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.thelazybattley.macrotrack.core.platform.deviceName
import org.thelazybattley.macrotrack.data.backup.BACKUP_FORMAT
import org.thelazybattley.macrotrack.data.backup.BACKUP_VERSION
import org.thelazybattley.macrotrack.data.backup.BackupFile
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.domain.model.BackupException
import org.thelazybattley.macrotrack.domain.model.BackupInfo
import org.thelazybattley.macrotrack.domain.model.BackupProblem
import org.thelazybattley.macrotrack.domain.model.LastBackup
import org.thelazybattley.macrotrack.domain.repository.BackupRepository
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class BackupRepositoryImpl(
    private val backupDao: BackupDao,
    private val appFlagDao: AppFlagDao
) : BackupRepository {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun createBackup(): ByteArray {
        val file = BackupFile(
            createdAtMillis = Clock.System.now().toEpochMilliseconds(),
            device = deviceName(),
            foods = backupDao.getFoods(),
            recipes = backupDao.getRecipes(),
            userDetails = backupDao.getUserDetails(),
            foodLogs = backupDao.getFoodLogs(),
            weights = backupDao.getWeights(),
            measurements = backupDao.getMeasurements(),
            dailyGoals = backupDao.getDailyGoals(),
            favorites = backupDao.getFavorites(),
            appFlags = backupDao.getAppFlags().filter { it.key !in deviceFlagKeys }
        )
        return json.encodeToString(serializer = BackupFile.serializer(), value = file).encodeToByteArray()
    }

    override suspend fun readBackup(bytes: ByteArray): BackupInfo {
        val file = decode(bytes = bytes)
        return BackupInfo(
            createdAtMillis = file.createdAtMillis,
            device = file.device,
            sizeBytes = bytes.size,
            foodLogCount = file.foodLogs.size,
            weightCount = file.weights.size
        )
    }

    override suspend fun restore(bytes: ByteArray) {
        backupDao.replaceAll(snapshot = decode(bytes = bytes).toSnapshot(), deviceFlagKeys = deviceFlagKeys)
    }

    override fun getLastBackup(): Flow<LastBackup?> = combine(
        flow = appFlagDao.getFlagValue(key = LAST_BACKUP_AT),
        flow2 = appFlagDao.getFlagValue(key = LAST_BACKUP_SIZE)
    ) { at, size ->
        at?.toLongOrNull()?.let { LastBackup(atMillis = it, sizeBytes = size?.toIntOrNull() ?: 0) }
    }

    @OptIn(ExperimentalTime::class)
    override suspend fun recordBackup(sizeBytes: Int) {
        appFlagDao.insertFlag(flag = AppFlagEntity(key = LAST_BACKUP_AT, value = Clock.System.now().toEpochMilliseconds().toString()))
        appFlagDao.insertFlag(flag = AppFlagEntity(key = LAST_BACKUP_SIZE, value = sizeBytes.toString()))
    }

    /** Checks the format and version before reading the rest, so a newer file says so instead of failing oddly. */
    private fun decode(bytes: ByteArray): BackupFile {
        val text = bytes.decodeToString()
        val header = try {
            json.parseToJsonElement(string = text).jsonObject
        } catch (_: Exception) {
            throw BackupException(problem = BackupProblem.NOT_A_BACKUP)
        }
        if (header["format"]?.jsonPrimitive?.content != BACKUP_FORMAT) throw BackupException(problem = BackupProblem.NOT_A_BACKUP)
        val version = header["version"]?.jsonPrimitive?.intOrNull ?: throw BackupException(problem = BackupProblem.NOT_A_BACKUP)
        if (version > BACKUP_VERSION) throw BackupException(problem = BackupProblem.TOO_NEW)
        return try {
            json.decodeFromString(deserializer = BackupFile.serializer(), string = text)
        } catch (_: SerializationException) {
            throw BackupException(problem = BackupProblem.NOT_A_BACKUP)
        } catch (_: IllegalArgumentException) {
            throw BackupException(problem = BackupProblem.NOT_A_BACKUP)
        }
    }

    companion object {
        const val LAST_BACKUP_AT = "backup_last_at"
        const val LAST_BACKUP_SIZE = "backup_last_size"

        /** Flags that belong to this phone: never written to a backup, and kept when one is restored. */
        val deviceFlagKeys = listOf(
            ProRepositoryImpl.PRO_TRIAL_START,
            ProRepositoryImpl.PRO_PURCHASED,
            ProRepositoryImpl.TRIAL_ENDED_NOTICE_DISMISSED,
            LAST_BACKUP_AT,
            LAST_BACKUP_SIZE,
            // The stores count rating prompts per install, so a restore must not reset or copy them.
            AppFlagRepositoryImpl.REVIEW_LAST_ASKED,
            AppFlagRepositoryImpl.REVIEW_TIMES_ASKED
        )
    }
}
