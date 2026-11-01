package org.thelazybattley.macrotrack.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.widget_setup_add
import macrotrack.composeapp.generated.resources.widget_setup_app
import macrotrack.composeapp.generated.resources.widget_setup_app_sub
import macrotrack.composeapp.generated.resources.widget_setup_big
import macrotrack.composeapp.generated.resources.widget_setup_calories
import macrotrack.composeapp.generated.resources.widget_setup_colours
import macrotrack.composeapp.generated.resources.widget_setup_hint
import macrotrack.composeapp.generated.resources.widget_setup_protein
import macrotrack.composeapp.generated.resources.widget_setup_title
import macrotrack.composeapp.generated.resources.widget_setup_wallpaper
import macrotrack.composeapp.generated.resources.widget_setup_wallpaper_sub
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/**
 * Opens when a widget is placed, and from its pencil later: what the Today ring shows, and whose colours a
 * widget uses. Only the choices a phone and widget have are shown.
 */
class WidgetSetupActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val appWidgetId = intent?.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID)
            ?: AppWidgetManager.INVALID_APPWIDGET_ID
        // Backing out before saving leaves the widget off the home screen, as the phone expects.
        setResult(RESULT_CANCELED, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        val providerName = AppWidgetManager.getInstance(this).getAppWidgetInfo(appWidgetId)?.provider?.className
        val widget: GlanceAppWidget = when (providerName) {
            TodayWidgetReceiver::class.java.name -> TodayWidget()
            WaterWidgetReceiver::class.java.name -> WaterWidget()
            WeightWidgetReceiver::class.java.name -> WeightWidget()
            StreakWidgetReceiver::class.java.name -> StreakWidget()
            else -> QuickLogWidget()
        }
        val isToday = widget is TodayWidget
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(context = this@WidgetSetupActivity).getGlanceIdBy(appWidgetId = appWidgetId)
            val prefs = getAppWidgetState<Preferences>(context = this@WidgetSetupActivity, definition = PreferencesGlanceStateDefinition, glanceId = glanceId)
            setContent {
                MacroTrackTheme {
                    val scope = rememberCoroutineScope()
                    SetupScreen(
                        hasBigNumber = isToday,
                        initialProtein = prefs[WidgetConfig.BIG_PROTEIN] == true,
                        initialAppColours = prefs[WidgetConfig.APP_COLOURS] == true,
                        onCancel = { finish() },
                        onSave = { isProtein, isAppColours ->
                            scope.launch {
                                updateAppWidgetState(context = this@WidgetSetupActivity, glanceId = glanceId) { state ->
                                    state[WidgetConfig.BIG_PROTEIN] = isProtein
                                    state[WidgetConfig.APP_COLOURS] = isAppColours
                                }
                                widget.update(context = this@WidgetSetupActivity, id = glanceId)
                                setResult(RESULT_OK, Intent().putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId))
                                finish()
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun SetupScreen(
    hasBigNumber: Boolean,
    initialProtein: Boolean,
    initialAppColours: Boolean,
    onCancel: () -> Unit,
    onSave: (isProtein: Boolean, isAppColours: Boolean) -> Unit
) {
    var isProtein by remember { mutableStateOf(value = initialProtein) }
    var isAppColours by remember { mutableStateOf(value = initialAppColours) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 20.dp)
        ) {
            Text(text = stringResource(resource = Res.string.widget_setup_title), style = typography.bold24, color = colors.ink)
            if (hasBigNumber) {
                Section(title = Res.string.widget_setup_big) {
                    Option(label = Res.string.widget_setup_calories, isSelected = !isProtein, onSelect = { isProtein = false })
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    Option(label = Res.string.widget_setup_protein, isSelected = isProtein, onSelect = { isProtein = true })
                }
            }
            if (hasWallpaperColours) {
                Section(title = Res.string.widget_setup_colours) {
                    Option(
                        label = Res.string.widget_setup_wallpaper,
                        detail = Res.string.widget_setup_wallpaper_sub,
                        isSelected = !isAppColours,
                        onSelect = { isAppColours = false }
                    )
                    HorizontalDivider(thickness = 1.dp, color = colors.divider)
                    Option(
                        label = Res.string.widget_setup_app,
                        detail = Res.string.widget_setup_app_sub,
                        isSelected = isAppColours,
                        onSelect = { isAppColours = true }
                    )
                }
            }
            Text(text = stringResource(resource = Res.string.widget_setup_hint), style = typography.regular13, color = colors.muted)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End)
        ) {
            SetupButton(label = Res.string.cancel, isPrimary = false, onClick = onCancel)
            SetupButton(label = Res.string.widget_setup_add, isPrimary = true, onClick = { onSave(isProtein, isAppColours) })
        }
    }
}

@Composable
private fun Section(title: StringResource, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = stringResource(resource = title), style = typography.bold12, color = colors.muted)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .selectableGroup()
                .clip(shape = RoundedCornerShape(size = 18.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
        ) {
            content()
        }
    }
}

@Composable
private fun Option(label: StringResource, isSelected: Boolean, onSelect: () -> Unit, detail: StringResource? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size = 20.dp)
                .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.faint, shape = CircleShape)
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = label), style = typography.bold14, color = colors.ink)
            detail?.let { Text(text = stringResource(resource = it), style = typography.regular11, color = colors.muted) }
        }
    }
}

@Composable
private fun SetupButton(label: StringResource, isPrimary: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(height = 44.dp)
            .clip(shape = RoundedCornerShape(size = 22.dp))
            .background(color = if (isPrimary) colors.accent else colors.background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(resource = label),
            style = typography.bold14,
            color = if (isPrimary) colors.onAccent else colors.accent
        )
    }
}
