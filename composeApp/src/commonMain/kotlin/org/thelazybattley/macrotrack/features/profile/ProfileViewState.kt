package org.thelazybattley.macrotrack.features.profile

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.LastBackup
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.UserDetails

data class ProfileViewState(
    val userDetails: UserDetails? = null,
    val macroGoals: MacroGoals? = null,
    val goalProgress: GoalProgress? = null,
    val weekSummary: WeekSummary? = null,
    val firstWeightDate: LocalDate? = null,
    val lastBackup: LastBackup? = null,
    val isTelemetryEnabled: Boolean = true,
    val route: String? = null
)

data class GoalProgress(
    val startWeight: Double,
    val startDate: LocalDate?,
    val currentWeight: Double,
    val targetWeight: Double,
    val isGaining: Boolean,
    val achievedKg: Double,
    val totalKg: Double,
    val progress: Float,
    val status: GoalStatus,
    val weeklyChange: Double?,
    val estimatedDate: LocalDate?
)

enum class GoalStatus {
    ON_TRACK,
    OFF_TRACK,
    NEEDS_MORE_DATA,
    REACHED
}

data class WeekSummary(
    val weekStart: LocalDate,
    val loggedDays: List<Boolean>,
    val todayIndex: Int,
    val currentStreak: Int,
    val bestStreak: Int,
    val averageCalories: Int?,
    val averageProtein: Int?,
    val daysOnTarget: Int,
    val daysLogged: Int,
    val weightChange: Double?
)
