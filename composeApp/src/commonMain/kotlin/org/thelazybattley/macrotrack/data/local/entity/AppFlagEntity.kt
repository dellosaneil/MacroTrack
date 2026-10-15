package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A one-off app setting. Most are either set (row present) or not, like "the first-run tour was seen";
 * some also keep a [value], like the day the Pro trial started.
 */
@Entity
data class AppFlagEntity(
    @PrimaryKey val key: String,
    val value: String? = null
)
