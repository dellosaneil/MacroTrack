package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.dao.ChecklistDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.data.local.entity.toEntity
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings
import org.thelazybattley.macrotrack.domain.repository.ChecklistRepository

/** Settings are kept as JSON in app flags, so an unreadable value falls back to the defaults. */
class ChecklistRepositoryImpl(private val checklistDao: ChecklistDao, private val flagDao: AppFlagDao) : ChecklistRepository {

    private val json = Json { ignoreUnknownKeys = true }

    override fun getItems(): Flow<List<ChecklistItem>> = checklistDao.getItems().map { items -> items.map { it.toDomain() } }

    override suspend fun getItem(id: Long): ChecklistItem? = checklistDao.getItem(id = id)?.toDomain()

    override suspend fun saveItem(item: ChecklistItem): Long = checklistDao.insertItem(item = item.toEntity())

    override suspend fun saveItems(items: List<ChecklistItem>) = checklistDao.updateItems(items = items.map { it.toEntity() })

    override suspend fun deleteItem(id: Long) {
        checklistDao.deleteEntriesOf(itemId = id)
        checklistDao.deleteItem(id = id)
    }

    override fun getEntries(): Flow<List<ChecklistEntry>> = checklistDao.getEntries().map { entries -> entries.map { it.toDomain() } }

    override suspend fun getEntry(itemId: Long, date: LocalDate): ChecklistEntry? =
        checklistDao.getEntry(itemId = itemId, date = date)?.toDomain()

    override suspend fun saveEntry(entry: ChecklistEntry) = checklistDao.insertEntry(entry = entry.toEntity())

    override suspend fun deleteEntry(itemId: Long, date: LocalDate) = checklistDao.deleteEntry(itemId = itemId, date = date)

    override fun getSettings(): Flow<ChecklistSettings> = flagDao.getFlagValue(key = CHECKLIST_SETTINGS).map { value ->
        value?.let { runCatching { json.decodeFromString<ChecklistSettings>(string = it) }.getOrNull() } ?: ChecklistSettings()
    }

    override suspend fun saveSettings(settings: ChecklistSettings) {
        flagDao.insertFlag(flag = AppFlagEntity(key = CHECKLIST_SETTINGS, value = json.encodeToString(value = settings)))
    }

    companion object {
        const val CHECKLIST_SETTINGS = "checklist_settings"
    }
}
