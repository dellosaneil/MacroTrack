package org.thelazybattley.macrotrack.widget

import android.content.Context
import android.content.Intent
import androidx.glance.GlanceId
import androidx.glance.action.Action
import androidx.glance.action.ActionParameters
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
import org.thelazybattley.macrotrack.domain.usecase.water.AddWaterUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.GetWaterSettingsUseCase
import org.thelazybattley.macrotrack.domain.usecase.water.ScheduleWaterRemindersUseCase
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import kotlinx.coroutines.flow.first

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

    /** Adds a glass of water without opening the app. */
    val addGlass: Action get() = actionRunCallback<AddGlassAction>()
}

/** The water button: logs one glass of the size set in Water settings, then redraws the widgets. */
class AddGlassAction : ActionCallback {

    override suspend fun onAction(context: Context, glanceId: GlanceId, parameters: ActionParameters) {
        val koin = GlobalContext.getOrNull() ?: return
        val glassMl = koin.get<GetWaterSettingsUseCase>()().first().glassMl
        koin.get<AddWaterUseCase>()(amountMl = glassMl, kind = WaterKind.GLASS)
        koin.get<HomeWidgetRefresher>().refresh()
        // Catching up on water takes back reminders that are no longer due.
        koin.get<ScheduleWaterRemindersUseCase>()()
    }
}
