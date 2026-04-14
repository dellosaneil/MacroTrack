package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserGender

@Entity
@Serializable
data class UserDetailsEntity(
    @PrimaryKey val id: Int = 0,
    val weight: Double,
    val age: Int,
    val height: Double,
    val gender: UserGender,
    val activityLevel: ActivityLevel,
    val goal: Goal
)

fun UserDetailsEntity.toDomain() = UserDetails(
    weight = weight,
    age = age,
    height = height,
    gender = gender,
    activityLevel = activityLevel,
    goal = goal
)
