package org.thelazybattley.macrotrack.widget

import android.appwidget.AppWidgetManager
import android.content.Intent
import android.os.Bundle
import android.widget.FrameLayout
import android.widget.RemoteViews
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.mutablePreferencesOf
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.compose
import androidx.glance.appwidget.state.getAppWidgetState
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.widget_detail_eaten
import macrotrack.composeapp.generated.resources.widget_log_food
import macrotrack.composeapp.generated.resources.widget_scan
import macrotrack.composeapp.generated.resources.widget_setup_add
import macrotrack.composeapp.generated.resources.widget_setup_app
import macrotrack.composeapp.generated.resources.widget_setup_app_sub
import macrotrack.composeapp.generated.resources.widget_setup_big
import macrotrack.composeapp.generated.resources.widget_setup_buttons
import macrotrack.composeapp.generated.resources.widget_setup_calories
import macrotrack.composeapp.generated.resources.widget_setup_chip_water
import macrotrack.composeapp.generated.resources.widget_setup_colours
import macrotrack.composeapp.generated.resources.widget_setup_count
import macrotrack.composeapp.generated.resources.widget_setup_details
import macrotrack.composeapp.generated.resources.widget_setup_hint
import macrotrack.composeapp.generated.resources.widget_setup_preview
import macrotrack.composeapp.generated.resources.widget_setup_protein
import macrotrack.composeapp.generated.resources.widget_setup_save
import macrotrack.composeapp.generated.resources.widget_setup_title
import macrotrack.composeapp.generated.resources.widget_setup_title_other
import macrotrack.composeapp.generated.resources.widget_setup_title_water
import macrotrack.composeapp.generated.resources.widget_setup_wallpaper
import macrotrack.composeapp.generated.resources.widget_setup_wallpaper_sub
import macrotrack.composeapp.generated.resources.widget_setup_water_amount
import macrotrack.composeapp.generated.resources.widget_streak
import macrotrack.composeapp.generated.resources.widget_water
import macrotrack.composeapp.generated.resources.widget_weight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.domain.model.WIDGET_MAX_DETAILS
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_AMOUNTS_ML
import org.thelazybattley.macrotrack.domain.model.WidgetButton
import org.thelazybattley.macrotrack.domain.model.WidgetDetail
import org.thelazybattley.macrotrack.domain.model.toggledUpTo
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase
import org.thelazybattley.macrotrack.features.widgets.toContent
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** Which choices a widget's setup offers. */
private enum class SetupKind { TODAY, WATER, OTHER }

/** What the person has picked so far; saved into the widget's own state on Add widget. */
private data class SetupChoices(
    val isProtein: Boolean,
    val details: List<WidgetDetail>,
    val buttons: List<WidgetButton>,
    val waterMl: Int,
    val isAppColours: Boolean
) {
    fun writeTo(prefs: androidx.datastore.preferences.core.MutablePreferences) {
        prefs[WidgetConfig.BIG_PROTEIN] = isProtein
        prefs[WidgetConfig.DETAILS] = details.encodeNames()
        prefs[WidgetConfig.BUTTONS] = buttons.encodeNames()
        prefs[WidgetConfig.WATER_ML] = waterMl
        prefs[WidgetConfig.APP_COLOURS] = isAppColours
    }

    fun toPreferences(): Preferences = mutablePreferencesOf().also { writeTo(prefs = it) }
}

/**
 * Opens when a widget is placed, and from its pencil later. Today picks its big number, details, buttons and how
 * much the water button adds; Water picks the amount; every widget picks whose colours it uses, on phones that have
 * wallpaper colours. The widget above the choices shows them as they're picked.
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
        val kind = when (widget) {
            is TodayWidget -> SetupKind.TODAY
            is WaterWidget -> SetupKind.WATER
            else -> SetupKind.OTHER
        }
        lifecycleScope.launch {
            val glanceId = GlanceAppWidgetManager(context = this@WidgetSetupActivity).getGlanceIdBy(appWidgetId = appWidgetId)
            val prefs = getAppWidgetState<Preferences>(context = this@WidgetSetupActivity, definition = PreferencesGlanceStateDefinition, glanceId = glanceId)
            val content = GlobalContext.get().get<GetWidgetSnapshotUseCase>()().first().toContent()
            val initial = SetupChoices(
                isProtein = prefs[WidgetConfig.BIG_PROTEIN] == true,
                details = prefs.details(),
                buttons = prefs.buttons(),
                waterMl = prefs.waterMl(),
                isAppColours = prefs[WidgetConfig.APP_COLOURS] == true
            )
            setContent {
                MacroTrackTheme {
                    val scope = rememberCoroutineScope()
                    SetupScreen(
                        kind = kind,
                        widget = widget,
                        initial = initial,
                        isEditing = prefs.asMap().isNotEmpty(),
                        waterLabels = WIDGET_WATER_AMOUNTS_ML.associateWith { content.waterAmount(ml = it).label },
                        onCancel = { finish() },
                        onSave = { choices ->
                            scope.launch {
                                updateAppWidgetState(context = this@WidgetSetupActivity, glanceId = glanceId) { state -> choices.writeTo(prefs = state) }
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SetupScreen(
    kind: SetupKind,
    widget: GlanceAppWidget,
    initial: SetupChoices,
    isEditing: Boolean,
    waterLabels: Map<Int, String>,
    onCancel: () -> Unit,
    onSave: (SetupChoices) -> Unit
) {
    var choices by remember { mutableStateOf(value = initial) }
    val isToday = kind == SetupKind.TODAY
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 64.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            val cancel = stringResource(resource = Res.string.cancel)
            Box(
                modifier = Modifier
                    .size(size = 48.dp)
                    .clip(shape = CircleShape)
                    .clickable(role = Role.Button, onClick = onCancel)
                    .semantics { contentDescription = cancel },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = CloseIcon, contentDescription = null, tint = colors.ink, modifier = Modifier.size(size = 22.dp))
            }
            Text(
                text = stringResource(
                    resource = when (kind) {
                        SetupKind.TODAY -> Res.string.widget_setup_title
                        SetupKind.WATER -> Res.string.widget_setup_title_water
                        SetupKind.OTHER -> Res.string.widget_setup_title_other
                    }
                ),
                style = typography.bold18,
                color = colors.ink
            )
        }
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .verticalScroll(state = rememberScrollState())
        ) {
            WidgetPreview(kind = kind, widget = widget, choices = choices)
            Column(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 8.dp),
                verticalArrangement = Arrangement.spacedBy(space = 18.dp)
            ) {
                if (isToday) {
                    Group(title = stringResource(resource = Res.string.widget_setup_big)) {
                        Segmented(
                            options = listOf(stringResource(resource = Res.string.widget_setup_calories), stringResource(resource = Res.string.widget_setup_protein)),
                            selected = if (choices.isProtein) 1 else 0,
                            onSelect = { choices = choices.copy(isProtein = it == 1) }
                        )
                    }
                    Group(
                        title = stringResource(resource = Res.string.widget_setup_details),
                        count = stringResource(resource = Res.string.widget_setup_count, choices.details.size, WIDGET_MAX_DETAILS)
                    ) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                            WidgetDetail.entries.forEach { detail ->
                                val isOn = detail in choices.details
                                Chip(
                                    label = stringResource(resource = detail.label),
                                    isOn = isOn,
                                    isEnabled = isOn || choices.details.size < WIDGET_MAX_DETAILS,
                                    onToggle = { choices = choices.copy(details = choices.details.toggledUpTo(item = detail, max = WIDGET_MAX_DETAILS)) }
                                )
                            }
                        }
                    }
                    Group(
                        title = stringResource(resource = Res.string.widget_setup_buttons),
                        count = stringResource(resource = Res.string.widget_setup_count, choices.buttons.size, WIDGET_MAX_BUTTONS)
                    ) {
                        FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 8.dp), verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                            WidgetButton.entries.forEach { button ->
                                val isOn = button in choices.buttons
                                Chip(
                                    label = stringResource(resource = button.label),
                                    isOn = isOn,
                                    isEnabled = isOn || choices.buttons.size < WIDGET_MAX_BUTTONS,
                                    onToggle = { choices = choices.copy(buttons = choices.buttons.toggledUpTo(item = button, max = WIDGET_MAX_BUTTONS)) }
                                )
                            }
                        }
                    }
                }
                if (kind == SetupKind.WATER || (isToday && WidgetButton.WATER in choices.buttons)) {
                    Group(title = stringResource(resource = Res.string.widget_setup_water_amount)) {
                        Segmented(
                            options = WIDGET_WATER_AMOUNTS_ML.map { waterLabels[it] ?: "$it ml" },
                            selected = WIDGET_WATER_AMOUNTS_ML.indexOf(choices.waterMl),
                            onSelect = { choices = choices.copy(waterMl = WIDGET_WATER_AMOUNTS_ML[it]) }
                        )
                    }
                }
                if (hasWallpaperColours) {
                    Group(title = stringResource(resource = Res.string.widget_setup_colours)) {
                        Column(modifier = Modifier.selectableGroup()) {
                            val isDark = isSystemInDarkTheme()
                            ColourOption(
                                label = Res.string.widget_setup_wallpaper,
                                detail = Res.string.widget_setup_wallpaper_sub,
                                swatch = if (isDark) Color(color = 0xFF1E2622) else Color(color = 0xFFE3EDE6),
                                isSelected = !choices.isAppColours,
                                onSelect = { choices = choices.copy(isAppColours = false) }
                            )
                            ColourOption(
                                label = Res.string.widget_setup_app,
                                detail = Res.string.widget_setup_app_sub,
                                swatch = if (isDark) Color(color = 0xFF1C1D20) else Color.White,
                                isSelected = choices.isAppColours,
                                onSelect = { choices = choices.copy(isAppColours = true) }
                            )
                        }
                    }
                }
                Text(text = stringResource(resource = Res.string.widget_setup_hint), style = typography.regular13, color = colors.muted)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.End)
        ) {
            SetupButton(label = Res.string.cancel, isPrimary = false, onClick = onCancel)
            SetupButton(
                label = if (isEditing) Res.string.widget_setup_save else Res.string.widget_setup_add,
                isPrimary = true,
                onClick = { onSave(choices) }
            )
        }
    }
}

/**
 * The widget itself, drawn the way the home screen will draw it with what's logged today and the choices so far,
 * on a soft wallpaper-like backdrop.
 */
@Composable
private fun WidgetPreview(kind: SetupKind, widget: GlanceAppWidget, choices: SetupChoices) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val isDark = isSystemInDarkTheme()
    val wallpaper = if (isDark) {
        listOf(Color(color = 0xFF11201A), Color(color = 0xFF0F1714), Color(color = 0xFF16201C))
    } else {
        listOf(Color(color = 0xFFCFE3D6), Color(color = 0xFFE4ECDD), Color(color = 0xFFD9E6E0))
    }
    val description = stringResource(resource = Res.string.widget_setup_preview)
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(shape = RoundedCornerShape(size = 28.dp))
            .background(brush = Brush.linearGradient(colors = wallpaper))
            .padding(vertical = 19.dp)
            .semantics { contentDescription = description },
        contentAlignment = Alignment.Center
    ) {
        val width = when (kind) {
            SetupKind.TODAY, SetupKind.OTHER -> minOf(maxWidth - 24.dp, 358.dp)
            SetupKind.WATER -> 173.dp
        }
        val size = when {
            kind == SetupKind.TODAY -> DpSize(width = width, height = 176.dp)
            kind == SetupKind.WATER || widget !is QuickLogWidget -> DpSize(width = 173.dp, height = 173.dp)
            else -> DpSize(width = width, height = 84.dp)
        }
        var views by remember { mutableStateOf<RemoteViews?>(value = null) }
        LaunchedEffect(choices, size) {
            views = runCatching { widget.compose(context = context, size = size, state = choices.toPreferences()) }.getOrNull()
        }
        AndroidView(
            factory = { FrameLayout(it) },
            update = { frame ->
                frame.removeAllViews()
                views?.let { frame.addView(it.apply(frame.context, frame)) }
            },
            modifier = Modifier.size(width = size.width, height = size.height)
        )
    }
}

@Composable
private fun Group(title: String, count: String? = null, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
            Text(text = title, style = typography.bold14, color = colors.accentInk, modifier = Modifier.weight(weight = 1f))
            count?.let { Text(text = it, style = typography.regular11, color = colors.muted) }
        }
        content()
    }
}

/** Options side by side in one outlined bar, the chosen one filled. */
@Composable
private fun Segmented(options: List<String>, selected: Int, onSelect: (Int) -> Unit) {
    val shape = RoundedCornerShape(size = 20.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = 44.dp)
            .clip(shape = shape)
            .border(width = 1.dp, color = colors.faint, shape = shape)
            .selectableGroup()
    ) {
        options.forEachIndexed { index, option ->
            if (index > 0) Box(modifier = Modifier.size(width = 1.dp, height = 44.dp).background(color = colors.faint))
            val isSelected = index == selected
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 44.dp)
                    .background(color = if (isSelected) colors.accentSoft else Color.Transparent)
                    .selectable(selected = isSelected, role = Role.RadioButton, onClick = { onSelect(index) }),
                contentAlignment = Alignment.Center
            ) {
                Text(text = option, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink)
            }
        }
    }
}

/** A filter chip: ticked and filled when on, faded when there's no room to turn it on. */
@Composable
private fun Chip(label: String, isOn: Boolean, isEnabled: Boolean, onToggle: () -> Unit) {
    val shape = RoundedCornerShape(size = 10.dp)
    Row(
        modifier = Modifier
            .height(height = 36.dp)
            .alpha(alpha = if (isEnabled) 1f else 0.38f)
            .clip(shape = shape)
            .then(if (isOn) Modifier.background(color = colors.accentSoft) else Modifier.border(width = 1.dp, color = colors.faint, shape = shape))
            .toggleable(value = isOn, enabled = isEnabled, role = Role.Checkbox, onValueChange = { onToggle() })
            .padding(start = if (isOn) 10.dp else 14.dp, end = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        if (isOn) Icon(imageVector = CheckIcon, contentDescription = null, tint = colors.accentInk, modifier = Modifier.size(size = 16.dp))
        Text(text = label, style = typography.bold14, color = if (isOn) colors.accentInk else colors.ink)
    }
}

@Composable
private fun ColourOption(label: StringResource, detail: StringResource, swatch: Color, isSelected: Boolean, onSelect: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .selectable(selected = isSelected, role = Role.RadioButton, onClick = onSelect)
            .padding(horizontal = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(size = 20.dp)
                .border(width = if (isSelected) 6.dp else 2.dp, color = if (isSelected) colors.accent else colors.faint, shape = CircleShape)
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = label), style = typography.bold14, color = colors.ink)
            Text(text = stringResource(resource = detail), style = typography.regular11, color = colors.muted)
        }
        Box(
            modifier = Modifier
                .size(size = 28.dp)
                .clip(shape = CircleShape)
                .background(color = swatch)
                .border(width = 2.dp, color = colors.accent, shape = CircleShape)
        )
    }
}

@Composable
private fun SetupButton(label: StringResource, isPrimary: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .height(height = 40.dp)
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = if (isPrimary) colors.accent else colors.background)
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = if (isPrimary) 24.dp else 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(resource = label),
            style = typography.bold14,
            color = if (isPrimary) colors.onAccent else colors.accent
        )
    }
}

private val WidgetDetail.label: StringResource
    get() = when (this) {
        WidgetDetail.PROTEIN -> Res.string.protein
        WidgetDetail.CARBS -> Res.string.carbs
        WidgetDetail.FAT -> Res.string.fat
        WidgetDetail.WATER -> Res.string.widget_water
        WidgetDetail.EATEN -> Res.string.widget_detail_eaten
        WidgetDetail.WEIGHT -> Res.string.widget_weight
        WidgetDetail.STREAK -> Res.string.widget_streak
    }

private val WidgetButton.label: StringResource
    get() = when (this) {
        WidgetButton.FOOD -> Res.string.widget_log_food
        WidgetButton.SCAN -> Res.string.widget_scan
        WidgetButton.WATER -> Res.string.widget_setup_chip_water
        WidgetButton.WEIGHT -> Res.string.widget_weight
    }

private val CloseIcon: ImageVector = ImageVector.Builder(name = "Close", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
    .path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.2f, strokeLineCap = StrokeCap.Round) {
        moveTo(6f, 6f)
        lineTo(18f, 18f)
        moveTo(18f, 6f)
        lineTo(6f, 18f)
    }
    .build()

private val CheckIcon: ImageVector = ImageVector.Builder(name = "Check", defaultWidth = 24.dp, defaultHeight = 24.dp, viewportWidth = 24f, viewportHeight = 24f)
    .path(stroke = SolidColor(Color.Black), strokeLineWidth = 2.6f, strokeLineCap = StrokeCap.Round, strokeLineJoin = StrokeJoin.Round) {
        moveTo(5f, 12.5f)
        lineTo(9.5f, 17f)
        lineTo(19f, 7.5f)
    }
    .build()
