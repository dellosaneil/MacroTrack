package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.LocalDate
import kotlin.math.roundToInt

/**
 * Calories burned by moving, on top of resting burn. Everything is an estimate from the user's profile:
 *
 * - Walks: ACSM walking equation, 0.1 mL O₂ per kg per metre on the flat plus 1.8 mL per kg per metre climbed.
 * - Runs: ACSM running equation, 0.2 mL per kg per metre plus 0.9 mL per kg per metre climbed.
 * - Everyday steps: walked on flat ground, each step [stepLengthMeters] long.
 *
 * About 5 kcal per litre of oxygen, so mL × kg ÷ 200 is kcal.
 */
object ActivityCalories {

    /** Step length as a share of height; women's steps are a touch shorter. */
    private const val STEP_LENGTH_MALE = 0.415
    private const val STEP_LENGTH_FEMALE = 0.413

    const val WALK_ML_PER_KG_METER = 0.1
    const val WALK_ML_PER_KG_CLIMB = 1.8
    const val RUN_ML_PER_KG_METER = 0.2
    const val RUN_ML_PER_KG_CLIMB = 0.9
    const val KCAL_PER_LITER_O2 = 5.0

    /** kcal per kg per km of flat walking: 0.1 mL × 1000 m ÷ 200. */
    const val FLAT_WALK_KCAL_PER_KG_KM = 0.5

    fun stepLengthFactor(sex: UserSex): Double = if (sex == UserSex.MALE) STEP_LENGTH_MALE else STEP_LENGTH_FEMALE

    fun stepLengthMeters(heightCm: Double, sex: UserSex): Double = heightCm * stepLengthFactor(sex = sex) / 100

    /** Oxygen used per kg, in mL, for [meters] covered and [climbMeters] climbed. */
    fun oxygenPerKg(type: ActivityType, meters: Double, climbMeters: Double): Double = when (type) {
        ActivityType.WALK -> WALK_ML_PER_KG_METER * meters + WALK_ML_PER_KG_CLIMB * climbMeters
        ActivityType.RUN -> RUN_ML_PER_KG_METER * meters + RUN_ML_PER_KG_CLIMB * climbMeters
    }

    fun sessionKcal(type: ActivityType, meters: Double, climbMeters: Double, weightKg: Double): Int =
        (oxygenPerKg(type = type, meters = meters, climbMeters = climbMeters) * weightKg / 1000 * KCAL_PER_LITER_O2).roundToInt()

    fun everydayKcal(steps: Int, stepLengthMeters: Double, weightKg: Double): Int =
        (FLAT_WALK_KCAL_PER_KG_KM * weightKg * steps * stepLengthMeters / METERS_PER_KM).roundToInt()

    /** Mifflin–St Jeor, the same resting burn the daily target starts from. */
    fun restingBurn(weightKg: Double, heightCm: Double, age: Int, sex: UserSex): Int =
        (10 * weightKg + 6.25 * heightCm - 5 * age + if (sex == UserSex.MALE) 5 else -161).roundToInt()
}

/** What the profile says about the user, for working out burn. */
data class BurnProfile(
    val weightKg: Double,
    val heightCm: Double,
    val age: Int,
    val sex: UserSex,
    /** The day of the weigh-in [weightKg] comes from; null when it's the profile's weight. */
    val weightDate: LocalDate? = null
) {
    val stepLengthMeters: Double get() = ActivityCalories.stepLengthMeters(heightCm = heightCm, sex = sex)

    val restingBurn: Int get() = ActivityCalories.restingBurn(weightKg = weightKg, heightCm = heightCm, age = age, sex = sex)

    fun kcal(session: ActivitySession): Int = ActivityCalories.sessionKcal(
        type = session.type,
        meters = session.distanceMeters,
        climbMeters = session.climbMeters,
        weightKg = weightKg
    )

    fun everydayKcal(steps: Int): Int =
        ActivityCalories.everydayKcal(steps = steps, stepLengthMeters = stepLengthMeters, weightKg = weightKg)
}

/** About 100 steps a minute at an easy walk. */
const val STEPS_PER_WALKING_MINUTE = 100

/**
 * One day's movement: every step the phone counted, and the walks and runs recorded. Steps taken during a
 * recording are counted by the phone too, so "everyday" steps are what's left once those are taken out.
 */
data class DayActivity(
    val totalSteps: Int,
    val sessions: List<ActivitySession>
) {
    val sessionSteps: Int get() = sessions.sumOf { it.steps }

    val everydaySteps: Int get() = (totalSteps - sessionSteps).coerceAtLeast(minimumValue = 0)

    /** Recorded distance plus everyday steps at the user's step length. */
    fun distanceMeters(profile: BurnProfile?): Double =
        sessions.sumOf { it.distanceMeters } + everydaySteps * (profile?.stepLengthMeters ?: DEFAULT_STEP_LENGTH_METERS)

    /** Recorded moving time plus everyday steps at an easy walk. */
    val activeMinutes: Int get() = sessions.sumOf { it.movingSeconds } / 60 + everydaySteps / STEPS_PER_WALKING_MINUTE

    fun sessionKcal(profile: BurnProfile): Int = sessions.sumOf { profile.kcal(session = it) }

    fun everydayKcal(profile: BurnProfile): Int = profile.everydayKcal(steps = everydaySteps)

    fun activeKcal(profile: BurnProfile): Int = sessionKcal(profile = profile) + everydayKcal(profile = profile)

    companion object {
        /** Used for distance before the profile is set up. */
        const val DEFAULT_STEP_LENGTH_METERS = 0.72
    }
}
