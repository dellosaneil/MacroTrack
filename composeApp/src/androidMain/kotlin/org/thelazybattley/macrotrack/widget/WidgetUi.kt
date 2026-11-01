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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import org.koin.core.context.GlobalContext
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.domain.usecase.widget.GetWidgetSnapshotUseCase

/** What each placed widget was set up to show, kept per widget. */
internal object WidgetConfig {
    /** The ring shows protein left instead of calories left. Today only. */
    val BIG_PROTEIN = booleanPreferencesKey(name = "big_protein")

    /** The widget uses the app's colours instead of the wallpaper's. */
    val APP_COLOURS = booleanPreferencesKey(name = "app_colours")
}

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
            val prefs = currentState<Preferences>()
            GlanceTheme {
                Content(content = content, prefs = prefs)
            }
        }
    }

    override suspend fun providePreview(context: Context, widgetCategory: Int) {
        val content = sampleSnapshot().toContent()
        provideContent {
            GlanceTheme {
                Content(content = content, prefs = null)
            }
        }
    }

    /** [prefs] is this widget's [WidgetConfig], null in the widget list. */
    @Composable
    abstract fun Content(content: WidgetContent, prefs: Preferences?)
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

@Composable
internal fun WidgetMacroBar(macro: MacroText, color: ColorProvider, palette: WidgetPalette) {
    Column(modifier = GlanceModifier.fillMaxWidth()) {
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            WidgetText(text = macro.name, color = palette.ink, size = 11.sp, isBold = true, modifier = GlanceModifier.defaultWeight())
            WidgetText(text = macro.value, color = palette.muted, size = 11.sp)
        }
        Spacer(modifier = GlanceModifier.height(height = 3.dp))
        LinearProgressIndicator(
            progress = macro.progress,
            modifier = GlanceModifier.fillMaxWidth().height(height = 6.dp).cornerRadius(radius = 3.dp),
            color = color,
            backgroundColor = palette.track
        )
    }
}

/** A rounded button with an icon and a label. */
@Composable
internal fun WidgetPill(
    label: String,
    icon: Int,
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
        Image(
            provider = ImageProvider(resId = icon),
            contentDescription = null,
            colorFilter = ColorFilter.tint(colorProvider = content),
            modifier = GlanceModifier.size(size = 14.dp)
        )
        Spacer(modifier = GlanceModifier.width(width = 4.dp))
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
