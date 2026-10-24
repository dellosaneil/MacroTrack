package org.thelazybattley.macrotrack.domain.model

import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ProgressPhotoTest {

    private val oct18 = LocalDate(2026, 10, 18)

    private fun photo(date: LocalDate, weightDate: LocalDate? = null, createdAtMillis: Long = 0, id: Long = 0) = ProgressPhoto(
        id = id,
        albumId = 1,
        date = date,
        pose = PhotoPose.FRONT,
        weightDate = weightDate,
        fileName = "f$id",
        createdAtMillis = createdAtMillis
    )

    @Test
    fun aPhotoShowsTheWeightLoggedOnItsDay() {
        val weights = mapOf(oct18 to 73.9)
        assertEquals(Weight(date = oct18, weight = 73.9), weightOnPhoto(photo = photo(date = oct18), weightsByDate = weights))
    }

    @Test
    fun aPickedNearbyWeighInStandsInForTheDay() {
        val oct17 = LocalDate(2026, 10, 17)
        val weights = mapOf(oct17 to 74.1)
        assertEquals(
            Weight(date = oct17, weight = 74.1),
            weightOnPhoto(photo = photo(date = oct18, weightDate = oct17), weightsByDate = weights)
        )
    }

    @Test
    fun aDayWithoutAWeighInShowsNoneUntilOneIsLogged() {
        assertNull(weightOnPhoto(photo = photo(date = oct18), weightsByDate = emptyMap()))
    }

    @Test
    fun theNearestWeighInPrefersTheDayBeforeOnATie() {
        val weights = mapOf(LocalDate(2026, 10, 16) to 74.0, LocalDate(2026, 10, 20) to 73.6, LocalDate(2026, 10, 17) to 74.1, LocalDate(2026, 10, 19) to 73.8)
        assertEquals(Weight(date = LocalDate(2026, 10, 17), weight = 74.1), nearestWeighIn(date = oct18, weightsByDate = weights))
    }

    @Test
    fun theNearestWeighInLooksNoFurtherThanThreeDays() {
        assertEquals(
            Weight(date = LocalDate(2026, 10, 21), weight = 73.5),
            nearestWeighIn(date = oct18, weightsByDate = mapOf(LocalDate(2026, 10, 21) to 73.5))
        )
        assertNull(nearestWeighIn(date = oct18, weightsByDate = mapOf(LocalDate(2026, 10, 14) to 74.5)))
    }

    @Test
    fun noNearbyWeighInIsOfferedWhenTheDayHasItsOwn() {
        assertNull(nearestWeighIn(date = oct18, weightsByDate = mapOf(oct18 to 73.9, LocalDate(2026, 10, 17) to 74.1)))
    }

    @Test
    fun theWaistShownIsTheLatestMeasuredByThatDay() {
        val measurements = listOf(
            Measurement(date = LocalDate(2026, 7, 6), type = MeasurementType.WAIST, valueCm = 88.0),
            Measurement(date = LocalDate(2026, 10, 4), type = MeasurementType.WAIST, valueCm = 84.5),
            Measurement(date = LocalDate(2026, 10, 10), type = MeasurementType.CHEST, valueCm = 102.0)
        )
        assertEquals(84.5, waistOn(date = oct18, measurements = measurements)?.valueCm)
        assertEquals(88.0, waistOn(date = LocalDate(2026, 9, 1), measurements = measurements)?.valueCm)
        assertNull(waistOn(date = LocalDate(2026, 7, 1), measurements = measurements))
    }

    @Test
    fun photosSortByDateThenByWhenTheyWereAdded() {
        val later = photo(date = oct18, createdAtMillis = 5, id = 2)
        val earlier = photo(date = oct18, createdAtMillis = 1, id = 3)
        val first = photo(date = LocalDate(2026, 9, 1), createdAtMillis = 9, id = 1)
        assertEquals(listOf(first, earlier, later), listOf(later, earlier, first).inDateOrder())
    }

    @Test
    fun reminderDaysSurviveBeingStoredAsText() {
        val days = setOf(DayOfWeek.THURSDAY, DayOfWeek.MONDAY)
        assertEquals("MONDAY,THURSDAY", days.toReminderText())
        assertEquals(days, reminderDaysOf(text = days.toReminderText()))
        assertEquals(emptySet(), reminderDaysOf(text = ""))
    }
}
