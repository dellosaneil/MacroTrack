package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity

data class UserDetails(
    val weight: Double,
    val age: Int,
    val height: Double,
    val sex: UserSex,
    val activityLevel: ActivityLevel,
    val goal: Goal
)

fun UserDetails.toEntity() = UserDetailsEntity(
    weight = weight,
    age = age,
    height = height,
    gender = sex,
    activityLevel = activityLevel,
    goal = goal
)
