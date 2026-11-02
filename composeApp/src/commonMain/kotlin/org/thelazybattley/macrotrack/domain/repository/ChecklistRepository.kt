package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistSettings

interface ChecklistRepository {

    /** In the user's order. */
    fun getItems(): Flow<List<ChecklistItem>>

    suspend fun getItem(id: Long): ChecklistItem?

    /** Returns the item's id, new when [item] has none yet. */
    suspend fun saveItem(item: ChecklistItem): Long

    suspend fun saveItems(items: List<ChecklistItem>)

    /** Takes away the item and its history. */
    suspend fun deleteItem(id: Long)

    fun getEntries(): Flow<List<ChecklistEntry>>

    suspend fun getEntry(itemId: Long, date: LocalDate): ChecklistEntry?

    suspend fun saveEntry(entry: ChecklistEntry)

    suspend fun deleteEntry(itemId: Long, date: LocalDate)

    fun getSettings(): Flow<ChecklistSettings>

    suspend fun saveSettings(settings: ChecklistSettings)
}
