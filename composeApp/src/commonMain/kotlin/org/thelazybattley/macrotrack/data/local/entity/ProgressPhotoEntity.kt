package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.PhotoAlbum
import org.thelazybattley.macrotrack.domain.model.PhotoPose
import org.thelazybattley.macrotrack.domain.model.ProgressPhoto
import org.thelazybattley.macrotrack.domain.model.reminderDaysOf
import org.thelazybattley.macrotrack.domain.model.toReminderText

/** Added by AppDatabase.MIGRATION_17_18. Photo albums stay on the phone and aren't in backups. */
@Entity
data class PhotoAlbumEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** Like "MONDAY,THURSDAY"; empty for no reminders. */
    val reminderDays: String,
    /** Minutes after midnight; added by AppDatabase.MIGRATION_18_19. */
    val reminderMinutes: Int,
    val isDateAndWeightShown: Boolean,
    val createdAtMillis: Long
)

/** Added by AppDatabase.MIGRATION_17_18. The image itself is a file named [fileName] in the app's photo folder. */
@Entity
data class ProgressPhotoEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val albumId: Long,
    val date: LocalDate,
    val takenOn: LocalDate?,
    val pose: PhotoPose,
    val note: String?,
    val weightDate: LocalDate?,
    val fileName: String,
    val createdAtMillis: Long
)

fun PhotoAlbumEntity.toDomain() = PhotoAlbum(
    id = id,
    name = name,
    reminderDays = reminderDaysOf(text = reminderDays),
    reminderMinutes = reminderMinutes,
    isDateAndWeightShown = isDateAndWeightShown,
    createdAtMillis = createdAtMillis
)

fun PhotoAlbum.toEntity() = PhotoAlbumEntity(
    id = id,
    name = name,
    reminderDays = reminderDays.toReminderText(),
    reminderMinutes = reminderMinutes,
    isDateAndWeightShown = isDateAndWeightShown,
    createdAtMillis = createdAtMillis
)

fun ProgressPhotoEntity.toDomain() = ProgressPhoto(
    id = id,
    albumId = albumId,
    date = date,
    takenOn = takenOn,
    pose = pose,
    note = note,
    weightDate = weightDate,
    fileName = fileName,
    createdAtMillis = createdAtMillis
)

fun ProgressPhoto.toEntity() = ProgressPhotoEntity(
    id = id,
    albumId = albumId,
    date = date,
    takenOn = takenOn,
    pose = pose,
    note = note,
    weightDate = weightDate,
    fileName = fileName,
    createdAtMillis = createdAtMillis
)
