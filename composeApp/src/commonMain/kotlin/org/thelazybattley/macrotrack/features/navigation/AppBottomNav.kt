package org.thelazybattley.macrotrack.features.navigation

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.features.pro.LocalProAccess
import org.thelazybattley.macrotrack.features.pro.ProAccess
import org.thelazybattley.macrotrack.features.pro.ProFeature
import org.thelazybattley.macrotrack.features.pro.ProViewModel
import org.thelazybattley.macrotrack.features.pro.proFeatureFor
import org.thelazybattley.macrotrack.features.pro.ui.PaywallScreen
import org.thelazybattley.macrotrack.features.tour.LocalTourTargets
import org.thelazybattley.macrotrack.features.tour.TourScreen
import org.thelazybattley.macrotrack.features.tour.TourTargetRegistry
import org.thelazybattley.macrotrack.features.tour.TourViewModel
import org.thelazybattley.macrotrack.features.tour.ui.TourOverlay
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import org.thelazybattley.macrotrack.features.addmeal.ui.AddMealScreen
import org.thelazybattley.macrotrack.features.calendar.ui.CalendarScreen
import org.thelazybattley.macrotrack.features.createfood.ui.CreateFoodScreen
import org.thelazybattley.macrotrack.features.foodlibrary.ui.FoodLibraryScreen
import org.thelazybattley.macrotrack.features.quickadd.ui.QuickAddScreen
import org.thelazybattley.macrotrack.features.scanbarcode.ui.ScanBarcodeScreen
import org.thelazybattley.macrotrack.features.createrecipe.ui.CreateRecipeScreen
import org.thelazybattley.macrotrack.features.logweight.ui.LogWeightScreen
import org.thelazybattley.macrotrack.features.measurements.detail.MeasurementDetailScreen
import org.thelazybattley.macrotrack.features.measurements.log.MeasurementsLogScreen
import org.thelazybattley.macrotrack.features.measurements.overview.MeasurementsScreen
import org.thelazybattley.macrotrack.features.onboarding.ui.OnboardingScreen
import org.thelazybattley.macrotrack.features.profile.nutritiongoals.ui.NutritionGoalsScreen
import org.thelazybattley.macrotrack.features.profile.pastgoal.ui.PastGoalScreen
import org.thelazybattley.macrotrack.features.profile.personalinformation.ui.PersonalInformationScreen
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.MovingAverageScreen
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.WeightHistoryScreen
import org.thelazybattley.macrotrack.features.splash.ui.SplashScreen
import org.thelazybattley.macrotrack.features.walkthrough.ui.WalkthroughScreen
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.FOOD_NAME
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEAL_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.BARCODE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.LIBRARY_FOOD
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.PREFILL_NAME
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.QUERY
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.MEASUREMENT_TYPE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.PRO_FEATURE
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations.Companion.RECIPE_NAME
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.domain.usecase.units.GetUnitSystemUseCase
import org.thelazybattley.macrotrack.ui.common.LocalUnitSystem
import org.koin.compose.koinInject
import androidx.compose.ui.text.intl.Locale
import org.thelazybattley.macrotrack.domain.model.UnitSystem

@Composable
@Preview(showBackground = true)
fun AppBottomNav() {
    val navController = rememberNavController()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val tourViewModel = koinViewModel<TourViewModel>()
    val tourState by tourViewModel.state.collectAsStateWithLifecycle()
    val tourTargets = remember { TourTargetRegistry() }
    var tabRequest by remember { mutableStateOf<TabRequest?>(value = null) }
    val proViewModel = koinViewModel<ProViewModel>()
    val proState by proViewModel.state.collectAsStateWithLifecycle()
    val getUnitSystem = koinInject<GetUnitSystemUseCase>()
    val unitSystemFlow = remember { getUnitSystem() }
    val unitSystem by unitSystemFlow.collectAsStateWithLifecycle(
        initialValue = UnitSystem.defaultFor(region = Locale.current.region)
    )
    val openPaywall: (ProFeature?) -> Unit = { feature ->
        navController.navigate(route = AppDestinations.Root.Paywall.createRoute(feature = feature?.name))
    }
    // Every screen-to-screen move goes through here, so a Pro screen opens the paywall once the trial is over.
    val navigate: (String) -> Unit = { route ->
        val feature = proFeatureFor(route = route, recipeCount = proState.recipeCount)
        if (feature != null && !proState.status.isActive) {
            openPaywall(feature)
        } else {
            navController.navigate(route = route)
        }
    }
    SideEffect {
        tourTargets.activeTarget = tourState.step?.target
    }
    LaunchedEffect(key1 = tourState.step?.screen) {
        val screen = tourState.step?.screen ?: return@LaunchedEffect
        navController.showTourScreen(screen = screen) { tab -> tabRequest = TabRequest(tab = tab) }
    }
    LaunchedEffect(key1 = tourState.outcome) {
        if (tourState.outcome == null) return@LaunchedEffect
        navController.showTourScreen(screen = TourScreen.LOG) { tab -> tabRequest = TabRequest(tab = tab) }
    }
    MacroTrackTheme {
        CompositionLocalProvider(
            LocalTourTargets provides tourTargets,
            LocalProAccess provides ProAccess(state = proState, openPaywall = openPaywall, callbacks = proViewModel),
            LocalUnitSystem provides unitSystem
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                NavHost(
                    navController = navController,
                    startDestination = AppDestinations.Root.Splash.route,
                    modifier = Modifier.pointerInput(key1 = Unit) {
                        detectTapGestures {
                            focusManager.clearFocus()
                            keyboardController?.hide()
                        }
                    }
                ) {
                    composable(route = AppDestinations.Root.Walkthrough.route) {
                        WalkthroughScreen {
                            navController.navigate(route = AppDestinations.Root.Onboarding.route) {
                                popUpTo(route = AppDestinations.Root.Walkthrough.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                    composable(route = AppDestinations.Root.Onboarding.route) {
                        OnboardingScreen(
                            modifier = Modifier
                        ) {
                            navController.navigate(route = AppDestinations.Root.MainScreen.route) {
                                popUpTo(route = AppDestinations.Root.Onboarding.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                    composable(route = AppDestinations.Root.MainScreen.route) {
                        LaunchedEffect(key1 = Unit) {
                            tourViewModel.onMainScreenShown()
                        }
                        MainScreenNavigation(
                            modifier = Modifier.fillMaxSize(),
                            tabRequest = tabRequest,
                            onReplayTour = tourViewModel::onReplayClicked
                        ) { destination ->
                            navigate(destination)
                        }
                    }
                    composable(route = AppDestinations.Root.Splash.route) {
                        SplashScreen { destination ->
                            navController.navigate(
                                route = destination.route
                            ) {
                                popUpTo(route = AppDestinations.Root.Splash.route) {
                                    inclusive = true
                                }
                            }
                        }
                    }
                    composable(
                        route = AppDestinations.Root.AddMeal.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = MEAL_TYPE) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        AddMealScreen(
                            modifier = Modifier,
                            onBackButtonPressed = {
                                navController.popBackStack()
                            }
                        ) { route ->
                            navigate(route)
                        }
                    }
                    composable(
                        route = AppDestinations.Root.ScanBarcode.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = MEAL_TYPE) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        ScanBarcodeScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigate = { route ->
                                navigate(route)
                            },
                            onSaved = {
                                navController.popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.CreateFood.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = FOOD_NAME) {
                                type = NavType.StringType
                            },
                            navArgument(name = BARCODE) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument(name = PREFILL_NAME) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            },
                            navArgument(name = LIBRARY_FOOD) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        CreateFoodScreen(
                            modifier = Modifier
                        ) {
                            navController.popBackStack()
                        }
                    }
                    composable(
                        route = AppDestinations.Root.FoodLibrary.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = MEAL_TYPE) {
                                type = NavType.StringType
                            },
                            navArgument(name = QUERY) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) {
                        FoodLibraryScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onNavigate = { route ->
                                navigate(route)
                            },
                            onSaved = {
                                navController.popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.QuickAdd.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = MEAL_TYPE) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        QuickAddScreen(
                            onBack = {
                                navController.popBackStack()
                            },
                            onSaved = {
                                navController.popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.CreateRecipe.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = RECIPE_NAME) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        CreateRecipeScreen(
                            modifier = Modifier
                        ) {
                            navController.popBackStack()
                        }
                    }

                    composable(
                        route = AppDestinations.Profile.PersonalInfo.route
                    ) {
                        PersonalInformationScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Profile.NutritionGoals.route
                    ) {
                        NutritionGoalsScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                            onNavigateToPersonalInformation = {
                                navController.navigate(route = AppDestinations.Profile.PersonalInfo.route)
                            },
                            onNavigateToPastGoal = {
                                navController.navigate(route = AppDestinations.Root.PastGoal.route)
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.PastGoal.route
                    ) {
                        PastGoalScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Profile.BodyMeasurements.route
                    ) {
                        MeasurementsScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                            onNavigate = navigate
                        )
                    }
                    composable(
                        route = AppDestinations.Root.MeasurementsLog.route
                    ) {
                        MeasurementsLogScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.MeasurementDetail.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = MEASUREMENT_TYPE) {
                                type = NavType.StringType
                            }
                        )
                    ) {
                        MeasurementDetailScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                            onNavigate = navigate
                        )
                    }
                    composable(
                        route = AppDestinations.Profile.WeightHistory.route
                    ) {
                        WeightHistoryScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                            onNavigate = navigate
                        )
                    }
                    composable(
                        route = AppDestinations.Root.MovingAverage.route
                    ) {
                        MovingAverageScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            },
                            onNavigate = navigate
                        )
                    }
                    composable(
                        route = AppDestinations.Root.Calendar.route
                    ) {
                        CalendarScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.Paywall.routeWithArgs!!,
                        arguments = listOf(
                            navArgument(name = PRO_FEATURE) {
                                type = NavType.StringType
                                nullable = true
                                defaultValue = null
                            }
                        )
                    ) { entry ->
                        val featureName = entry.savedStateHandle.get<String>(key = PRO_FEATURE)
                        PaywallScreen(
                            feature = ProFeature.entries.find { it.name == featureName },
                            viewState = proState,
                            callbacks = proViewModel,
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable(
                        route = AppDestinations.Root.LogWeight.route
                    ) {
                        LogWeightScreen(
                            modifier = Modifier,
                            onPopBackStack = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
                TourOverlay(
                    viewState = tourState,
                    targets = tourTargets,
                    callbacks = tourViewModel
                )
            }
        }
    }
}

/** Shows the screen a tour step talks about, starting from the main screen so Back never leads somewhere odd. */
private fun NavHostController.showTourScreen(
    screen: TourScreen,
    onSelectTab: (AppDestinations.BottomNavigation) -> Unit
) {
    val currentRoute = currentBackStackEntry?.destination?.route
    when (screen) {
        TourScreen.ADD_MEAL -> {
            if (currentRoute == AppDestinations.Root.AddMeal.routeWithArgs) return
            popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
            navigate(route = AppDestinations.Root.AddMeal.createRoute(mealType = MealType.BREAKFAST.name))
        }
        TourScreen.MOVING_AVERAGE -> {
            if (currentRoute == AppDestinations.Root.MovingAverage.route) return
            popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
            navigate(route = AppDestinations.Root.MovingAverage.route)
        }
        else -> {
            popBackStack(route = AppDestinations.Root.MainScreen.route, inclusive = false)
            screen.tab?.let(onSelectTab)
        }
    }
}

val AppPadding = PaddingValues(
    horizontal = 16.dp,
    vertical = 8.dp
)
