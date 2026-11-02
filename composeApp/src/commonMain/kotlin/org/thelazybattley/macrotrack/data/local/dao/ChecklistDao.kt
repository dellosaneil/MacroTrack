package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.entity.ChecklistEntryEntity
import org.thelazybattley.macrotrack.data.local.entity.ChecklistItemEntity

@Dao
interface ChecklistDao {

    /** In the user's order. */
    @Query("SELECT * FROM ChecklistItemEntity ORDER BY sortOrder, id")
    fun getItems(): Flow<List<ChecklistItemEntity>>

    @Query("SELECT * FROM ChecklistItemEntity WHERE id = :id")
    suspend fun getItem(id: Long): ChecklistItemEntity?

    /** Returns the item's id, new when [item] has none yet. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(item: ChecklistItemEntity): Long

    @Update
    suspend fun updateItems(items: List<ChecklistItemEntity>)

    @Query("DELETE FROM ChecklistItemEntity WHERE id = :id")
    suspend fun deleteItem(id: Long)

    @Query("SELECT * FROM ChecklistEntryEntity")
    fun getEntries(): Flow<List<ChecklistEntryEntity>>

    @Query("SELECT * FROM ChecklistEntryEntity WHERE itemId = :itemId AND date = :date")
    suspend fun getEntry(itemId: Long, date: LocalDate): ChecklistEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: ChecklistEntryEntity)

    @Query("DELETE FROM ChecklistEntryEntity WHERE itemId = :itemId AND date = :date")
    suspend fun deleteEntry(itemId: Long, date: LocalDate)

    @Query("DELETE FROM ChecklistEntryEntity WHERE itemId = :itemId")
    suspend fun deleteEntriesOf(itemId: Long)
}
