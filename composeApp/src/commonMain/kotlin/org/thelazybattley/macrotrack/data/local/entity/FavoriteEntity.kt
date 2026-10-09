package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/** A starred food or recipe, keyed like the Add Meal list ("food:Name" or "recipe:Name"). */
@Entity
data class FavoriteEntity(
    @PrimaryKey val key: String
)
