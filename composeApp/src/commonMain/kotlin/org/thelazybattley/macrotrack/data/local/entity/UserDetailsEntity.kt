package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.ActivityLevel
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.UserSex

@Entity
@Serializable
data class UserDetailsEntity(
    @PrimaryKey val id: Int = 0,
    val weight: Double,
    val age: Int,
    val height: Double,
    val gender: UserSex,
    val activityLevel: ActivityLevel,
    val goal: Goal
)

fun UserDetailsEntity.toDomain() = UserDetails(
    weight = weight,
    age = age,
    height = height,
    sex = gender,
    activityLevel = activityLevel,
    goal = goal
)
