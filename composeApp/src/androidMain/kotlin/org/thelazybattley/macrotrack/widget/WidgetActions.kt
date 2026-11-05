package org.thelazybattley.macrotrack.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
import androidx.glance.action.actionParametersOf
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.action.actionStartActivity
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.MainActivity
import org.thelazybattley.macrotrack.core.platform.AppShortcut
import org.thelazybattley.macrotrack.core.platform.EXTRA_APP_SHORTCUT
import org.thelazybattley.macrotrack.core.platform.HomeWidgetRefresher
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.WaterKind
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_ADDED_MILLIS
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_DEFAULT_ML
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_UNDONE_MILLIS
import org.thelazybattley.macrotrack.domain.usecase.water.AddWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.DeleteWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import kotlinx.coroutines.delay

/** On the intent that opens the app from a widget: the screen to show over the main one. */
const val EXTRA_WIDGET_ROUTE = "widget_route"

/** Brings the app forward, reusing it when it's already open, so the screen opens over where it was. */
private fun Context.appIntent(): Intent = Intent(this, MainActivity::class.java)
    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)

internal object WidgetActions {

    fun openApp(context: Context): Action = actionStartActivity(intent = context.appIntent())

    /** Like the shortcut on the app icon, so food goes to the meal of the time it's tapped, not when the widget drew. */
    fun shortcut(context: Context, shortcut: AppShortcut): Action =
        actionStartActivity(intent = context.appIntent().setAction("widget.${shortcut.name}").putExtra(EXTRA_APP_SHORTCUT, shortcut.name))

    fun addTo(context: Context, mealType: MealType): Action = actionStartActivity(
        intent = context.appIntent()
            .setAction("widget.add.${mealType.name}")
            .putExtra(EXTRA_WIDGET_ROUTE, AppDestinations.Root.AddMeal.createRoute(mealType = mealType.name))
    )

    /** Adds [ml] of water without opening the app. */
    fun addWater(ml: Int): Action = actionRunCallback<AddGlassAction>(parameters = actionParametersOf(WATER_ML to ml))

    /** Takes back the water just added from a widget. */
    val undoWater: Action get() = actionRunCallback<UndoWaterAction>()

    val WATER_ML = ActionParameters.Key<Int>(name = "water_ml")
}

/**
 * The water button: logs the amount the widget was set up with, and every widget showing water answers for
 * [WIDGET_WATER_ADDED_MILLIS] with the new total and Undo, then goes back to how it was.
 */
class AddGlassAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val koin = GlobalContext.getOrNull() ?: return
        val ml = parameters[WidgetActions.WATER_ML] ?: WIDGET_WATER_DEFAULT_ML
        val entry = koin.get<AddWaterUseCase>()(amountMl = ml, kind = WaterKind.GLASS)
        val token = WaterFeedback.show(entryId = entry.id, ml = ml, isUndone = false)
        val refresher = koin.get<HomeWidgetRefresher>()
        refresher.refresh()
        // Catching up on water takes back reminders that are no longer due.
        koin.get<ScheduleWaterRemindersUseCase>()()
        delay(timeMillis = WIDGET_WATER_ADDED_MILLIS)
        if (WaterFeedback.clear(token = token)) refresher.refresh()
    }
}

/** Undo on a widget: deletes the water just added, and the button says so for [WIDGET_WATER_UNDONE_MILLIS]. */
class UndoWaterAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val koin = GlobalContext.getOrNull() ?: return
        val tap = WaterFeedback.tap.value?.takeIf { it.isAdded } ?: return
        koin.get<DeleteWaterUseCase>()(id = tap.entryId)
        val token = WaterFeedback.show(entryId = tap.entryId, ml = tap.ml, isUndone = true)
        val refresher = koin.get<HomeWidgetRefresher>()
        refresher.refresh()
        koin.get<ScheduleWaterRemindersUseCase>()()
        delay(timeMillis = WIDGET_WATER_UNDONE_MILLIS)
        if (WaterFeedback.clear(token = token)) refresher.refresh()
    }
}
