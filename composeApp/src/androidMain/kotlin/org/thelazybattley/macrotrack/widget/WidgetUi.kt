package org.thelazybattley.macrotrack.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.action.Action
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.LinearProgressIndicator
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.model.WIDGET_DEFAULT_DETAILS
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_AMOUNTS_ML
import org.thelazybattley.macrotrack.domain.model.WIDGET_WATER_DEFAULT_ML
import org.thelazybattley.macrotrack.domain.model.WidgetButton
import org.thelazybattley.macrotrack.domain.model.WidgetDetail
import org.thelazybattley.macrotrack.features.widgets.DetailText
import org.thelazybattley.macrotrack.features.widgets.WidgetContent
import org.thelazybattley.macrotrack.features.widgets.toContent
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase

/** What each placed widget was set up to show, kept per widget. */
internal object WidgetConfig {
    /** The ring shows protein left instead of calories left. Today only. */
    val BIG_PROTEIN = booleanPreferencesKey(name = "big_protein")

    /** The widget uses the app's colours instead of the wallpaper's. */
    val APP_COLOURS = booleanPreferencesKey(name = "app_colours")

    /** The Today widget's details next to the ring, as [WidgetDetail] names in the order picked. */
    val DETAILS = stringPreferencesKey(name = "details")

    /** The Today widget's buttons, as [WidgetButton] names; empty for none. */
    val BUTTONS = stringPreferencesKey(name = "buttons")

    /** How much the water button adds, in ml. */
    val WATER_ML = intPreferencesKey(name = "water_ml")
}

/** Buttons fit three to a row on Android. */
internal const val WIDGET_MAX_BUTTONS = 3

internal val WIDGET_DEFAULT_BUTTONS = listOf(WidgetButton.FOOD, WidgetButton.SCAN, WidgetButton.WATER)

internal fun Preferences?.details(): List<WidgetDetail> = this?.get(WidgetConfig.DETAILS)?.let { decodeNames<WidgetDetail>(text = it) } ?: WIDGET_DEFAULT_DETAILS

internal fun Preferences?.buttons(): List<WidgetButton> = this?.get(WidgetConfig.BUTTONS)?.let { decodeNames<WidgetButton>(text = it) } ?: WIDGET_DEFAULT_BUTTONS

internal fun Preferences?.waterMl(): Int = this?.get(WidgetConfig.WATER_ML)?.takeIf { it in WIDGET_WATER_AMOUNTS_ML } ?: WIDGET_WATER_DEFAULT_ML

internal fun List<Enum<*>>.encodeNames(): String = joinToString(separator = ",") { it.name }

private inline fun <reified T : Enum<T>> decodeNames(text: String): List<T> =
    text.split(',').mapNotNull { name -> enumValues<T>().find { it.name == name } }.distinct()

/**
 * A widget drawn from today's snapshot, redrawn while it's on screen as anything logged changes. Before Android 15
 * the widget list shows the app icon; from 15 on it shows [Content] with sample numbers.
 */
internal abstract class SnapshotWidget : GlanceAppWidget() {

    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val contents = GlobalContext.get().get<GetWidgetSnapshotUseCase>()().map { it.toContent() }
        val first = contents.first()
        provideContent {
            val content by contents.collectAsState(initial = first)
            val tap by WaterFeedback.tap.collectAsState()
            val prefs = currentState<Preferences>()
            GlanceTheme {
                Content(content = content, prefs = prefs, tap = tap)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val content = sampleSnapshot().toContent()
        provideContent {
            GlanceTheme {
                Content(content = content, prefs = null, tap = null)
            }
        }
    }

    /** [prefs] is this widget's [WidgetConfig], null in the widget list. [tap] is the water button's feedback, if showing. */
    @Composable
    abstract fun Content(content: WidgetContent, prefs: Preferences?, tap: WaterTap?)
}

internal fun Preferences?.colours(): WidgetColours =
    if (this?.get(WidgetConfig.APP_COLOURS) == true || !hasWallpaperColours) WidgetColours.APP else WidgetColours.WALLPAPER

/** The widget's card: its background and corners, opening the app when tapped anywhere without its own action. */
@Composable
internal fun WidgetFrame(palette: WidgetPalette, padding: Dp = 16.dp, content: @Composable () -> Unit) {
    val context = LocalContext.current
    Box(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(colorProvider = palette.background)
            .cornerRadius(radius = 28.dp)
            .clickable(onClick = WidgetActions.openApp(context = context))
            .padding(all = padding)
    ) {
        content()
    }
}

@Composable
internal fun WidgetText(
    text: String,
    color: ColorProvider,
    size: TextUnit = 12.sp,
    isBold: Boolean = false,
    isNumber: Boolean = false,
    modifier: GlanceModifier = GlanceModifier,
    align: TextAlign = TextAlign.Start
) {
    Text(
        text = text,
        modifier = modifier,
        maxLines = 1,
        style = TextStyle(
            color = color,
            fontSize = size,
            fontWeight = if (isBold || isNumber) FontWeight.Bold else FontWeight.Medium,
            textAlign = align
        )
    )
}

/** The app mark and a title, top left of a widget. */
@Composable
internal fun WidgetHeader(title: String, palette: WidgetPalette, trailing: String? = null) {
    Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = GlanceModifier.size(size = 18.dp).background(colorProvider = palette.accent).cornerRadius(radius = 5.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(resId = R.drawable.ic_widget_logo),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorProvider = palette.onAccent),
                modifier = GlanceModifier.size(size = 11.dp)
            )
        }
        Spacer(modifier = GlanceModifier.width(width = 6.dp))
        WidgetText(text = title, color = palette.ink, size = 13.sp, isBold = true, modifier = GlanceModifier.defaultWeight())
        trailing?.let { WidgetText(text = it, color = palette.muted, size = 12.sp) }
    }
}

/** A progress ring with a number and a word in the middle. */
@Composable
internal fun WidgetRing(sizeDp: Int, progress: Float, number: String, label: String, palette: WidgetPalette) {
    val context = LocalContext.current
    val stroke = sizeDp / 10f
    Box(modifier = GlanceModifier.size(size = sizeDp.dp), contentAlignment = Alignment.Center) {
        Image(
            provider = ImageProvider(bitmap = WidgetBitmaps.ring(context = context, sizeDp = sizeDp, strokeDp = stroke, fraction = 1f)),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorProvider = palette.track),
            modifier = GlanceModifier.fillMaxSize()
        )
        Image(
            provider = ImageProvider(bitmap = WidgetBitmaps.ring(context = context, sizeDp = sizeDp, strokeDp = stroke, fraction = progress)),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorProvider = palette.accent),
            modifier = GlanceModifier.fillMaxSize()
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            WidgetText(text = number, color = palette.ink, size = (sizeDp / 4.3f).sp, isNumber = true)
            WidgetText(text = label, color = palette.muted, size = 10.sp)
        }
    }
}

/** A detail next to the Today ring: its name and value, and a bar unless it has no progress, like weight. */
@Composable
internal fun WidgetDetailBar(detail: DetailText, color: ColorProvider, palette: WidgetPalette, valueColor: ColorProvider = palette.muted) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            WidgetText(text = detail.name, color = palette.ink, size = 11.sp, isBold = true, modifier = GlanceModifier.defaultWeight())
            WidgetText(text = detail.value, color = valueColor, size = 11.sp, isBold = valueColor != palette.muted)
        }
        detail.progress?.let { progress ->
            Spacer(modifier = GlanceModifier.height(height = 3.dp))
            LinearProgressIndicator(
                progress = progress,
                modifier = GlanceModifier.fillMaxWidth().height(height = 6.dp).cornerRadius(radius = 3.dp),
                color = color,
                backgroundColor = palette.track
            )
        }
    }
}

/** The colour of a detail's bar. */
internal fun WidgetPalette.colorOf(detail: WidgetDetail): ColorProvider = when (detail) {
    WidgetDetail.PROTEIN -> protein
    WidgetDetail.CARBS -> carbs
    WidgetDetail.FAT -> fat
    WidgetDetail.WATER -> water
    WidgetDetail.EATEN, WidgetDetail.WEIGHT -> accent
    WidgetDetail.STREAK -> warm
}

/** A rounded button with an icon and a label. */
@Composable
internal fun WidgetPill(
    label: String,
    icon: Int?,
    background: ColorProvider,
    content: ColorProvider,
    onClick: Action,
    modifier: GlanceModifier = GlanceModifier
) {
    Row(
        modifier = modifier
            .height(height = 36.dp)
            .background(colorProvider = background)
            .cornerRadius(radius = 18.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        icon?.let {
            Image(
                provider = ImageProvider(resId = it),
                contentDescription = null,
                colorFilter = ColorFilter.tint(colorProvider = content),
                modifier = GlanceModifier.size(size = 14.dp)
            )
            Spacer(modifier = GlanceModifier.width(width = 4.dp))
        }
        WidgetText(text = label, color = content, size = 12.sp, isBold = true)
    }
}

/** Shown before onboarding is done, when there's no goal to measure against. */
@Composable
internal fun WidgetSetUp(content: WidgetContent, palette: WidgetPalette) {
    Column(modifier = GlanceModifier.fillMaxSize()) {
        WidgetHeader(title = content.today, palette = palette)
        Spacer(modifier = GlanceModifier.defaultWeight())
        Text(
            text = content.setUp,
            maxLines = 3,
            style = TextStyle(color = palette.muted, fontSize = 12.sp, fontWeight = FontWeight.Medium)
        )
    }
}
