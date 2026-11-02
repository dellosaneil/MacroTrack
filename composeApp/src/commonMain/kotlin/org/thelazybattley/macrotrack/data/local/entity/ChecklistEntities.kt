package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.ChecklistEntry
import org.thelazybattley.macrotrack.domain.model.ChecklistIcon
import org.thelazybattley.macrotrack.domain.model.ChecklistItem
import org.thelazybattley.macrotrack.domain.model.ChecklistKind
import org.thelazybattley.macrotrack.domain.model.ChecklistStatus
import org.thelazybattley.macrotrack.domain.model.ChecklistTime
import org.thelazybattley.macrotrack.domain.model.HabitGoal
import org.thelazybattley.macrotrack.domain.model.SupplementUnit

/** A supplement or habit on the checklist. [days] is weekday names, comma separated. */
@Entity
@Serializable
data class ChecklistItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val kind: String,
    val name: String,
    val icon: String,
    val amount: Double,
    val unit: String,
    val goal: String,
    val goalValue: Int,
    val time: String,
    val days: String,
    val reminderMinutes: Int?,
    val stock: Int?,
    val stockWarnAt: Int,
    val refills: Int,
    val sortOrder: Int,
    val pausedOn: LocalDate?,
    val createdOn: LocalDate
)

/** What happened to an item on a day: done (at [doneAt], epoch milliseconds), skipped, or some of a times habit. */
@Entity(primaryKeys = ["itemId", "date"], indices = [Index(value = ["date"])])
@Serializable
data class ChecklistEntryEntity(
    val itemId: Long,
    val date: LocalDate,
    val status: String,
    val count: Int,
    val doneAt: Long?
)

private inline fun <reified T : Enum<T>> enumOf(name: String, fallback: T): T = enumValues<T>().find { it.name == name } ?: fallback

fun ChecklistItemEntity.toDomain() = ChecklistItem(
    id = id,
    kind = enumOf(name = kind, fallback = ChecklistKind.SUPPLEMENT),
    name = name,
    icon = enumOf(name = icon, fallback = ChecklistIcon.PILL),
    amount = amount,
    unit = enumOf(name = unit, fallback = SupplementUnit.CAPSULES),
    goal = enumOf(name = goal, fallback = HabitGoal.TICK),
    goalValue = goalValue,
    time = enumOf(name = time, fallback = ChecklistTime.ANY_TIME),
    days = days.split(",").mapNotNull { day -> DayOfWeek.entries.find { it.name == day } }.toSet(),
    reminderMinutes = reminderMinutes,
    stock = stock,
    stockWarnAt = stockWarnAt,
    refills = refills,
    sortOrder = sortOrder,
    pausedOn = pausedOn,
    createdOn = createdOn
)

fun ChecklistItem.toEntity() = ChecklistItemEntity(
    id = id,
    kind = kind.name,
    name = name,
    icon = icon.name,
    amount = amount,
    unit = unit.name,
    goal = goal.name,
    goalValue = goalValue,
    time = time.name,
    days = DayOfWeek.entries.filter { it in days }.joinToString(separator = ",") { it.name },
    reminderMinutes = reminderMinutes,
    stock = stock,
    stockWarnAt = stockWarnAt,
    refills = refills,
    sortOrder = sortOrder,
    pausedOn = pausedOn,
    createdOn = createdOn
)

fun ChecklistEntryEntity.toDomain() = ChecklistEntry(
    itemId = itemId,
    date = date,
    status = enumOf(name = status, fallback = ChecklistStatus.DONE),
    count = count,
    doneAtMillis = doneAt
)

fun ChecklistEntry.toEntity() = ChecklistEntryEntity(itemId = itemId, date = date, status = status.name, count = count, doneAt = doneAtMillis)
