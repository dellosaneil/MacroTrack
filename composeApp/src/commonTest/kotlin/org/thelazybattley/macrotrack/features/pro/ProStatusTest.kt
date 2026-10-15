package org.thelazybattley.macrotrack.features.pro

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.domain.model.FREE_RECIPE_LIMIT
import org.thelazybattley.macrotrack.domain.model.ProStatus
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

class ProStatusTest {

    private val start = LocalDate(2026, 10, 1)

    @Test
    fun firstDayHasTheWholeTrialLeft() {
        val status = ProStatus(trialStart = start, today = start)
        assertEquals(30, status.trialDaysLeft)
        assertTrue(status.isTrialActive)
        assertTrue(status.isActive)
    }

    @Test
    fun lastTrialDayIsStillPro() {
        val status = ProStatus(trialStart = start, today = LocalDate(2026, 10, 30))
        assertEquals(1, status.trialDaysLeft)
        assertTrue(status.isActive)
        assertFalse(status.hasTrialEnded)
    }

    @Test
    fun thirtyDaysLaterTheTrialHasEnded() {
        val status = ProStatus(trialStart = start, today = LocalDate(2026, 10, 31))
        assertEquals(0, status.trialDaysLeft)
        assertFalse(status.isActive)
        assertTrue(status.hasTrialEnded)
    }

    @Test
    fun purchaseKeepsProAfterTheTrial() {
        val status = ProStatus(isPurchased = true, trialStart = start, today = LocalDate(2027, 3, 1))
        assertTrue(status.isActive)
        assertFalse(status.isTrialActive)
        assertFalse(status.hasTrialEnded)
    }

    @Test
    fun unknownTrialStartDoesNotLockAnything() {
        assertTrue(ProStatus(today = start).isActive)
    }

    @Test
    fun proScreensNeedPro() {
        assertEquals(ProFeature.MOVING_AVERAGE, proFeatureFor(route = AppDestinations.Root.MovingAverage.route, recipeCount = 0))
        assertEquals(ProFeature.BODY_MEASUREMENTS, proFeatureFor(route = AppDestinations.Root.MeasurementsLog.route, recipeCount = 0))
        assertNull(proFeatureFor(route = AppDestinations.Profile.BodyMeasurements.route, recipeCount = 0))
        assertNull(proFeatureFor(route = AppDestinations.Root.LogWeight.route, recipeCount = 0))
    }

    @Test
    fun newRecipesNeedProOnlyPastTheFreeLimit() {
        val newRecipe = AppDestinations.Root.CreateRecipe.createRoute(recipeName = "")
        assertNull(proFeatureFor(route = newRecipe, recipeCount = FREE_RECIPE_LIMIT - 1))
        assertEquals(ProFeature.RECIPES, proFeatureFor(route = newRecipe, recipeCount = FREE_RECIPE_LIMIT))
        val editRecipe = AppDestinations.Root.CreateRecipe.createRoute(recipeName = "Oats")
        assertNull(proFeatureFor(route = editRecipe, recipeCount = FREE_RECIPE_LIMIT + 5))
    }
}
