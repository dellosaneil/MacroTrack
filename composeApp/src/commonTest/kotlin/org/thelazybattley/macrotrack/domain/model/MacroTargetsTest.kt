package org.thelazybattley.macrotrack.domain.model

import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.usecase.CalculateMacrosGoalUseCase
import org.thelazybattley.macrotrack.domain.usecase.CalculateTDEEUseCase
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class MacroTargetsTest {

    private val calculate = CalculateMacrosGoalUseCase(calculateTDEEUseCase = CalculateTDEEUseCase())

    // The case from the bug report: carbs used to reach -54 g at 3 kg a month.
    private val user = UserDetails(
        weight = 100.0,
        age = 45,
        height = 165.0,
        sex = UserSex.FEMALE,
        activityLevel = ActivityLevel.SEDENTARY,
        goal = Goal.LOSE_WEIGHT,
        targetWeight = 70.0
    )

    @Test
    fun proteinAndFatComeFromTheTargetWeight() {
        val goals = calculate(userDetails = user)
        assertEquals(140, goals.protein)
        assertEquals(49, goals.fat)
    }

    @Test
    fun carbsTakeWhatIsLeftAndNeverGoBelowZero() {
        assertEquals(168, calculate(userDetails = user.copy(goalPace = 1.2)).carbs)
        assertEquals(115, calculate(userDetails = user.copy(goalPace = 2.0)).carbs)
        assertEquals(53, calculate(userDetails = user.copy(goalPace = 3.0)).carbs)
        assertEquals(0, calculate(userDetails = user.copy(goalPace = 4.0)).carbs)
    }

    @Test
    fun theBasisNeverGoesAboveTodaysWeight() {
        val gaining = user.copy(weight = 60.0, targetWeight = 70.0)
        assertEquals(60.0, MacroTargets.basisWeight(weight = 60.0, height = 165.0, targetWeight = 70.0, basis = MacroBasis.TARGET_WEIGHT, bodyFat = null))
        assertEquals(120, calculate(userDetails = gaining).protein)
    }

    @Test
    fun withoutATargetTheBasisIsTheHealthyWeight() {
        val basis = MacroTargets.basisWeight(weight = 100.0, height = 165.0, targetWeight = null, basis = MacroBasis.TARGET_WEIGHT, bodyFat = null)
        assertEquals(68.06, basis, absoluteTolerance = 0.01)
    }

    @Test
    fun leanMassNeedsABodyFat() {
        assertEquals(55.0, MacroTargets.basisWeight(weight = 100.0, height = 165.0, targetWeight = 70.0, basis = MacroBasis.LEAN_MASS, bodyFat = 45.0), absoluteTolerance = 1e-9)
        assertEquals(70.0, MacroTargets.basisWeight(weight = 100.0, height = 165.0, targetWeight = 70.0, basis = MacroBasis.LEAN_MASS, bodyFat = null))
    }

    @Test
    fun customTargetsReplaceTheCalculatedOnes() {
        val custom = MacroGoals(calories = 1500, protein = 140, carbs = 100, fat = 60)
        assertEquals(custom, calculate(userDetails = user.copy(customMacroGoals = custom)))
        assertEquals(168, calculate.calculated(userDetails = user.copy(customMacroGoals = custom)).carbs)
    }

    @Test
    fun theFastestPaceStillLeavesFiftyGramsOfCarbs() {
        val maintenance = 1974
        val pace = MacroTargets.fastestPaceKeepingCarbs(maintenance = maintenance, protein = 140, fat = 49)!!
        val carbs = calculate(userDetails = user.copy(goalPace = pace)).carbs
        assertTrue(carbs >= MacroTargets.LOW_CARB_GRAMS - 2, "carbs $carbs at $pace kg a month")
        assertTrue(calculate(userDetails = user.copy(goalPace = pace + 0.2)).carbs < MacroTargets.LOW_CARB_GRAMS)
        assertNull(MacroTargets.fastestPaceKeepingCarbs(maintenance = 1100, protein = 140, fat = 49))
    }

    @Test
    fun splitsFillTheCalorieGoal() {
        val keto = MacroSplit.KETO.goalsFor(calories = 2000)
        assertEquals(MacroGoals(calories = 2000, protein = 125, carbs = 25, fat = 156), keto)
        assertTrue(MacroSplit.KETO.matches(goals = keto))
        assertTrue(!MacroSplit.BALANCED.matches(goals = keto))
    }

    @Test
    fun carbsFillTheRestOfCustomCalories() {
        assertEquals(100, carbsFor(calories = 1500, protein = 140, fat = 60))
        assertEquals(0, carbsFor(calories = 900, protein = 140, fat = 60))
    }
}
