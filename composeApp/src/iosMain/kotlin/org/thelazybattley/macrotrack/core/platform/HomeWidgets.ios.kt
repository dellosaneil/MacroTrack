package org.thelazybattley.macrotrack.core.platform

import kotlinx.coroutines.MainScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import org.koin.mp.KoinPlatform
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.domain.usecase.water.AddWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase
import org.thelazybattley.macrotrack.features.widgets.toContent
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/**
 * Implemented in Swift, where WidgetKit is: keeps what the widgets show in the App Group they share with the app.
 * Only JSON strings cross over.
 */
interface WidgetBridge {

    /** Saves [json], an [IosWidgetData], and asks every widget to redraw. */
    fun save(json: String)

    /** The glasses added from the water button since last asked, as a JSON list of [PendingWater], then forgets them. */
    fun takePendingWater(): String
}

/** A glass added from a widget while the app was closed: how much, and when it was tapped. */
@Serializable
data class PendingWater(val ml: Int, val atMillis: Long)

private val json = Json { ignoreUnknownKeys = true }

class IosHomeWidgetRefresher(private val bridge: WidgetBridge) : HomeWidgetRefresher {

    private val mutex = Mutex()

    override val isSupported: Boolean = true

    override val platform: WidgetPlatform = WidgetPlatform.IOS

    override suspend fun refresh() = mutex.withLock {
        val koin = KoinPlatform.getKoin()
        val pending = runCatching { json.decodeFromString<List<PendingWater>>(string = bridge.takePendingWater()) }.getOrDefault(emptyList())
        if (pending.isNotEmpty()) {
            val addWater = koin.get<AddWaterUseCase>()
            pending.forEach { addWater(amountMl = it.ml, kind = WaterKind.GLASS, atMillis = it.atMillis) }
            koin.get<ScheduleWaterRemindersUseCase>()()
        }
        val snapshot = koin.get<GetWidgetSnapshotUseCase>()().first()
        val data = IosWidgetData(
            today = snapshot.toDay(),
            next = snapshot.nextDay().toDay(),
            isLockNumbersShown = snapshot.isLockNumbersShown
        )
        bridge.save(json = json.encodeToString(value = data))
    }
}

/** What the widgets show: today, and the day after for when midnight passes before the app opens again. */
@Serializable
data class IosWidgetData(val today: IosWidgetDay, val next: IosWidgetDay, val isLockNumbersShown: Boolean)

@Serializable
data class IosWidgetMacro(val name: String, val short: String, val value: String, val left: String, val progress: Float)

@Serializable
data class IosWidgetMeal(val url: String, val name: String, val detail: String, val calories: String?)

/** One day's widgets with every word already written; Swift only lays them out. Water stays in ml so a glass can be added. */
@Serializable
data class IosWidgetDay(
    val date: String,
    val isSetUp: Boolean,
    val title: String,
    val dateText: String,
    val setUp: String,
    val caloriesNumber: String,
    val caloriesLabel: String,
    val caloriesProgress: Float,
    val proteinNumber: String,
    val proteinLabel: String,
    val proteinProgress: Float,
    val proteinLeft: String,
    val eaten: String,
    val macros: List<IosWidgetMacro>,
    val meals: List<IosWidgetMeal>,
    val food: String,
    val scan: String,
    val addWater: String,
    val waterTitle: String,
    val waterMl: Int,
    val waterGoalMl: Int,
    val glassMl: Int,
    val isImperial: Boolean,
    val waterOf: String,
    val weightTitle: String,
    val hasWeight: Boolean,
    val weightNumber: String,
    val weightChange: String,
    val weightCaption: String,
    val weightEmpty: String,
    val weightLine: List<Double>,
    val streakTitle: String,
    val streakDays: String,
    val streakUnit: String,
    val streakNote: String,
    val streakLetters: List<String>,
    val streakStates: List<String>
)

private suspend fun WidgetSnapshot.toDay(): IosWidgetDay {
    val content = toContent()
    return IosWidgetDay(
        date = date.toString(),
        isSetUp = isSetUp,
        title = content.today,
        dateText = content.date,
        setUp = content.setUp,
        caloriesNumber = content.caloriesNumber,
        caloriesLabel = content.caloriesLabel,
        caloriesProgress = calories.progress,
        proteinNumber = content.proteinNumber,
        proteinLabel = content.proteinLabel,
        proteinProgress = protein.progress,
        proteinLeft = content.proteinLeft,
        eaten = content.eaten,
        macros = content.macros.map { IosWidgetMacro(name = it.name, short = it.name.take(n = 1), value = it.value, left = it.left, progress = it.progress) },
        meals = content.meals.zip(meals) { text, meal ->
            IosWidgetMeal(url = widgetUrl(mealType = meal.mealType), name = text.name, detail = text.detail, calories = text.calories)
        },
        food = content.food,
        scan = content.scan,
        addWater = content.addWater,
        waterTitle = content.waterTitle,
        waterMl = water.totalMl,
        waterGoalMl = water.goalMl,
        glassMl = water.glassMl,
        isImperial = unitSystem == UnitSystem.IMPERIAL,
        waterOf = content.waterOf,
        weightTitle = content.weightTitle,
        hasWeight = weight != null,
        weightNumber = content.weightNumber,
        weightChange = content.weightChange,
        weightCaption = content.weightCaption,
        weightEmpty = content.weightEmpty,
        weightLine = weight?.line.orEmpty(),
        streakTitle = content.streakTitle,
        streakDays = content.streakDays,
        streakUnit = content.streakUnit,
        streakNote = content.streakNote,
        streakLetters = content.dayLetters,
        streakStates = streak.week.map { it.state.name }
    )
}

private const val SCHEME = "fitmetric"

private fun widgetUrl(mealType: MealType): String = "$SCHEME://add/${mealType.name}"

/**
 * A link tapped on a widget: fitmetric://shortcut/LOG_FOOD works like that app icon shortcut, and
 * fitmetric://add/LUNCH opens Add meal for lunch. Any other fitmetric link just opens the app. Returns false for
 * links that aren't the app's.
 */
fun openWidgetUrl(url: String): Boolean {
    val parts = url.removePrefix(prefix = "$SCHEME://").takeIf { it != url }?.split('/') ?: return false
    when (parts.getOrNull(index = 0)) {
        "shortcut" -> openAppShortcut(type = parts.getOrNull(index = 1).orEmpty())
        "add" -> MealType.entries.find { it.name == parts.getOrNull(index = 1) }?.let { mealType ->
            requestRoute(route = AppDestinations.Root.AddMeal.createRoute(mealType = mealType.name))
        }
    }
    return true
}

/** Called as the app comes to the front: logs water added from a widget meanwhile, and catches the widgets up. */
fun refreshHomeWidgets() {
    val refresher = KoinPlatform.getKoinOrNull()?.getOrNull<HomeWidgetRefresher>() ?: return
    MainScope().launch { refresher.refresh() }
}
