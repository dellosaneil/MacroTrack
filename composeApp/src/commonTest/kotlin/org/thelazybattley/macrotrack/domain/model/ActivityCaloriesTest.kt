package org.thelazybattley.macrotrack.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals

class ActivityCaloriesTest {

    // The design's sample user: male, 29, 175 cm, 74.2 kg.
    private val profile = BurnProfile(weightKg = 74.2, heightCm = 175.0, age = 29, sex = UserSex.MALE)

    private val parkLoop = ActivitySession(
        type = ActivityType.WALK, startMillis = 0, endMillis = 2_850_000, movingSeconds = 2_850,
        distanceMeters = 4_150.0, steps = 5_420, climbMeters = 32.0
    )

    private val riverside = ActivitySession(
        type = ActivityType.RUN, startMillis = 0, endMillis = 2_160_000, movingSeconds = 2_160,
        distanceMeters = 6_100.0, steps = 5_810, climbMeters = 18.0
    )

    @Test
    fun walksAndRunsUseTheirOwnAcsmEquation() {
        assertEquals(175, profile.kcal(session = parkLoop))
        assertEquals(459, profile.kcal(session = riverside))
    }

    @Test
    fun everydayStepsAreFlatWalkingAtTheUsersStepLength() {
        assertEquals(0.72625, profile.stepLengthMeters, absoluteTolerance = 1e-9)
        assertEquals(0.413 * 1.6, ActivityCalories.stepLengthMeters(heightCm = 160.0, sex = UserSex.FEMALE), absoluteTolerance = 1e-9)
        assertEquals(46, profile.everydayKcal(steps = 1_720))
    }

    @Test
    fun aDayAddsEverydayStepsToTheRecordedOnes() {
        val day = DayActivity(totalSteps = 7_140, sessions = listOf(parkLoop))
        assertEquals(1_720, day.everydaySteps)
        assertEquals(221, day.activeKcal(profile = profile))
        // A recording can count more steps than the phone saved for the day so far; everyday steps never go negative.
        assertEquals(0, DayActivity(totalSteps = 3_000, sessions = listOf(parkLoop)).everydaySteps)
    }

    @Test
    fun restingBurnIsMifflinStJeor() {
        assertEquals(1_696, profile.restingBurn)
        assertEquals(1_530, profile.copy(sex = UserSex.FEMALE).restingBurn)
    }
}
