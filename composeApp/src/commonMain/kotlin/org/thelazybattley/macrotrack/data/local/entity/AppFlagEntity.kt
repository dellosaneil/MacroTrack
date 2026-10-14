package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A one-off app flag that is either set (row present) or not, like "the first-run tour was seen". */
@Entity
data class AppFlagEntity(
    @PrimaryKey val key: String
)
