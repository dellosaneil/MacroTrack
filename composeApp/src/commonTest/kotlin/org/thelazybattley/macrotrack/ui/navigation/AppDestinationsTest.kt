package org.thelazybattley.macrotrack.ui.navigation

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import kotlin.test.Test
import kotlin.test.assertEquals

class AppDestinationsTest {

    @Test
    fun createFoodRouteEncodesNames() {
        assertEquals(
            "create_food/?libraryFood=Milk%2C%202%25",
            AppDestinations.Root.CreateFood.createRoute(foodName = "", libraryFood = "Milk, 2%")
        )
        assertEquals(
            "create_food/Greek%20yogurt%2C%202%25",
            AppDestinations.Root.CreateFood.createRoute(foodName = "Greek yogurt, 2%")
        )
    }

    @Test
    fun createFoodRouteLeavesOutMissingArguments() {
        assertEquals("create_food/", AppDestinations.Root.CreateFood.createRoute(foodName = ""))
        assertEquals(
            "create_food/?barcode=4800016642507",
            AppDestinations.Root.CreateFood.createRoute(foodName = "", barcode = "4800016642507")
        )
        assertEquals(
            "create_food/?prefillName=Jalape%C3%B1o%20%26%20rice",
            AppDestinations.Root.CreateFood.createRoute(foodName = "", prefillName = "Jalapeño & rice")
        )
    }

    @Test
    fun foodLibraryRouteSkipsABlankQuery() {
        assertEquals("food_library/DINNER", AppDestinations.Root.FoodLibrary.createRoute(mealType = "DINNER", query = " "))
        assertEquals("food_library/DINNER?query=chicken", AppDestinations.Root.FoodLibrary.createRoute(mealType = "DINNER", query = "chicken"))
    }

    @Test
    fun logRoutesCarryTheDayToLogTo() {
        val sunday = LocalDate(year = 2026, month = 10, day = 4)
        assertEquals("add_meal/DINNER?logDate=2026-10-04", AppDestinations.Root.AddMeal.createRoute(mealType = "DINNER", logDate = sunday))
        assertEquals("quick_add/DINNER?logDate=2026-10-04", AppDestinations.Root.QuickAdd.createRoute(mealType = "DINNER", logDate = sunday))
        assertEquals("scan_barcode/DINNER?logDate=2026-10-04", AppDestinations.Root.ScanBarcode.createRoute(mealType = "DINNER", logDate = sunday))
        assertEquals(
            "food_library/DINNER?query=rice&logDate=2026-10-04",
            AppDestinations.Root.FoodLibrary.createRoute(mealType = "DINNER", query = "rice", logDate = sunday)
        )
        assertEquals("add_meal/BREAKFAST", AppDestinations.Root.AddMeal.createRoute(mealType = "BREAKFAST"))
    }

    @Test
    fun logDateFallsBackToToday() {
        assertEquals(LocalDate(year = 2026, month = 10, day = 4), logDateOf(argument = "2026-10-04"))
        assertEquals(getCurrentDate(), logDateOf(argument = null))
        assertEquals(getCurrentDate(), logDateOf(argument = "not a date"))
    }
}
