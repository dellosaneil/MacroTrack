package org.thelazybattley.macrotrack.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.Preferences
import androidx.glance.ColorFilter
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import org.thelazybattley.macrotrack.R
import org.thelazybattley.macrotrack.core.platform.AppShortcut
import org.thelazybattley.macrotrack.domain.model.StreakDayState

/** Calories left in a ring with the macros, and buttons to log. Made taller, it lists each meal too. */
internal class TodayWidget : SnapshotWidget() {

    override val sizeMode = SizeMode.Responsive(sizes = setOf(MEDIUM, LARGE))

    @Composable
    override fun Content(content: WidgetContent, prefs: Preferences?) {
        val palette = widgetPalette(colours = prefs.colours())
        val isProtein = prefs?.get(WidgetConfig.BIG_PROTEIN) == true
        WidgetFrame(palette = palette, padding = FRAME_PADDING.dp) {
            when {
                !content.snapshot.isSetUp -> WidgetSetUp(content = content, palette = palette)
                LocalSize.current.height >= LARGE.height -> Large(content = content, palette = palette, isProtein = isProtein)
                else -> Medium(content = content, palette = palette, isProtein = isProtein)
            }
        }
    }

    @Composable
    private fun Medium(content: WidgetContent, palette: WidgetPalette, isProtein: Boolean) {
        // What's left of a short widget after its padding and buttons, so the ring never pushes them off.
        val ringDp = (LocalSize.current.height.value - 2 * FRAME_PADDING - BUTTONS_HEIGHT).toInt().coerceIn(minimumValue = 56, maximumValue = 92)
        Column(modifier = GlanceModifier.fillMaxSize()) {
            Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.CenterVertically) {
                WidgetRing(
                    sizeDp = ringDp,
                    progress = content.bigProgress(isProtein = isProtein),
                    number = content.bigNumber(isProtein = isProtein),
                    label = content.bigLabel(isProtein = isProtein),
                    palette = palette
                )
                Spacer(modifier = GlanceModifier.width(width = 16.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Row(modifier = GlanceModifier.fillMaxWidth()) {
                        WidgetText(text = content.today, color = palette.ink, isBold = true, modifier = GlanceModifier.defaultWeight())
                        WidgetText(text = content.date, color = palette.muted)
                    }
                    Macros(content = content, palette = palette, gap = 4.dp)
                }
            }
            Spacer(modifier = GlanceModifier.height(height = 8.dp))
            Buttons(content = content, palette = palette)
        }
    }

    @Composable
    private fun Large(content: WidgetContent, palette: WidgetPalette, isProtein: Boolean) {
        val context = LocalContext.current
        Column(modifier = GlanceModifier.fillMaxSize()) {
            WidgetHeader(title = content.today, palette = palette, trailing = content.date)
            Spacer(modifier = GlanceModifier.height(height = 10.dp))
            Row(modifier = GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                WidgetRing(
                    sizeDp = 88,
                    progress = content.bigProgress(isProtein = isProtein),
                    number = content.bigNumber(isProtein = isProtein),
                    label = content.bigLabel(isProtein = isProtein),
                    palette = palette
                )
                Spacer(modifier = GlanceModifier.width(width = 16.dp))
                Column(modifier = GlanceModifier.defaultWeight()) {
                    Macros(content = content, palette = palette, gap = 6.dp)
                }
            }
            Spacer(modifier = GlanceModifier.height(height = 10.dp))
            Column(modifier = GlanceModifier.fillMaxWidth().defaultWeight()) {
                content.meals.zip(content.snapshot.meals).forEachIndexed { index, (meal, snapshotMeal) ->
                    if (index > 0) Spacer(modifier = GlanceModifier.height(height = 4.dp))
                    Row(
                        modifier = GlanceModifier
                            .fillMaxWidth()
                            .defaultWeight()
                            .background(colorProvider = palette.tonal)
                            .cornerRadius(radius = 16.dp)
                            .clickable(onClick = WidgetActions.addTo(context = context, mealType = snapshotMeal.mealType))
                            .padding(start = 12.dp, end = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = GlanceModifier.defaultWeight()) {
                            WidgetText(text = meal.name, color = palette.ink, size = 13.sp, isBold = true)
                            WidgetText(text = meal.detail, color = palette.muted, size = 11.sp)
                        }
                        if (meal.calories != null) {
                            WidgetText(text = meal.calories, color = palette.muted, size = 13.sp, isBold = true, modifier = GlanceModifier.padding(end = 6.dp))
                        } else {
                            Box(
                                modifier = GlanceModifier.size(size = 28.dp).background(colorProvider = palette.accent).cornerRadius(radius = 14.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    provider = ImageProvider(resId = R.drawable.ic_widget_add),
                                    contentDescription = meal.name,
                                    colorFilter = ColorFilter.tint(colorProvider = palette.onAccent),
                                    modifier = GlanceModifier.size(size = 16.dp)
                                )
                            }
                        }
                    }
                }
            }
            Spacer(modifier = GlanceModifier.height(height = 10.dp))
            Buttons(content = content, palette = palette)
        }
    }

    @Composable
    private fun Macros(content: WidgetContent, palette: WidgetPalette, gap: Dp) {
        val colors = listOf(palette.protein, palette.carbs, palette.fat)
        content.macros.zip(colors).forEach { (macro, color) ->
            Spacer(modifier = GlanceModifier.height(height = gap))
            WidgetMacroBar(macro = macro, color = color, palette = palette)
        }
    }

    @Composable
    private fun Buttons(content: WidgetContent, palette: WidgetPalette) {
        val context = LocalContext.current
        Row(modifier = GlanceModifier.fillMaxWidth()) {
            WidgetPill(
                label = content.logFood,
                icon = R.drawable.ic_widget_add,
                background = palette.accent,
                content = palette.onAccent,
                onClick = WidgetActions.shortcut(context = context, shortcut = AppShortcut.LOG_FOOD),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(width = 6.dp))
            WidgetPill(
                label = content.scan,
                icon = R.drawable.ic_widget_scan,
                background = palette.tonal,
                content = palette.tonalInk,
                onClick = WidgetActions.shortcut(context = context, shortcut = AppShortcut.SCAN_BARCODE),
                modifier = GlanceModifier.defaultWeight()
            )
            Spacer(modifier = GlanceModifier.width(width = 6.dp))
            WidgetPill(
                label = content.addWater,
                icon = R.drawable.ic_widget_water,
                background = palette.waterSoft,
                content = palette.water,
                onClick = WidgetActions.addGlass,
                modifier = GlanceModifier.defaultWeight()
            )
        }
    }

    companion object {
        val MEDIUM = DpSize(width = 250.dp, height = 110.dp)
        /** About 4×4; shorter than this, four meal rows get too cramped to read. */
        val LARGE = DpSize(width = 250.dp, height = 320.dp)
        private const val FRAME_PADDING = 14
        private const val BUTTONS_HEIGHT = 44
    }
}

/** Today's water against the goal, and a button that adds a glass without opening the app. */
internal class WaterWidget : SnapshotWidget() {

    @Composable
    override fun Content(content: WidgetContent, prefs: Preferences?) {
        val palette = widgetPalette(colours = prefs.colours())
        val water = content.snapshot.water
        WidgetFrame(palette = palette, padding = 14.dp) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(resId = R.drawable.ic_widget_water),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorProvider = palette.water),
                        modifier = GlanceModifier.size(size = 14.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(width = 6.dp))
                    WidgetText(text = content.waterTitle, color = palette.water, size = 13.sp, isBold = true)
                }
                Row(modifier = GlanceModifier.fillMaxWidth().defaultWeight(), verticalAlignment = Alignment.Bottom) {
                    Box(
                        modifier = GlanceModifier.width(width = 28.dp).height(height = GLASS_HEIGHT.dp).background(colorProvider = palette.waterSoft).cornerRadius(radius = 9.dp),
                        contentAlignment = Alignment.BottomCenter
                    ) {
                        Box(
                            modifier = GlanceModifier
                                .width(width = 28.dp)
                                .height(height = (GLASS_HEIGHT * water.progress).dp)
                                .background(colorProvider = palette.water)
                                .cornerRadius(radius = 9.dp)
                        ) {}
                    }
                    Spacer(modifier = GlanceModifier.width(width = 10.dp))
                    Column {
                        WidgetText(text = content.waterTotal, color = palette.ink, size = 24.sp, isNumber = true)
                        WidgetText(text = content.waterOf, color = palette.muted, size = 11.sp)
                    }
                }
                Spacer(modifier = GlanceModifier.height(height = 8.dp))
                WidgetPill(
                    label = content.addWater,
                    icon = R.drawable.ic_widget_water,
                    background = palette.waterSoft,
                    content = palette.water,
                    onClick = WidgetActions.addGlass,
                    modifier = GlanceModifier.fillMaxWidth()
                )
            }
        }
    }

    private companion object {
        const val GLASS_HEIGHT = 60
    }
}

/** The weight trend, how it moved this week, and the last week's line. */
internal class WeightWidget : SnapshotWidget() {

    @Composable
    override fun Content(content: WidgetContent, prefs: Preferences?) {
        val palette = widgetPalette(colours = prefs.colours())
        val context = LocalContext.current
        val weight = content.snapshot.weight
        WidgetFrame(palette = palette, padding = 14.dp) {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .clickable(onClick = WidgetActions.shortcut(context = context, shortcut = AppShortcut.LOG_WEIGHT))
            ) {
                WidgetHeader(title = content.weightTitle, palette = palette)
                if (weight == null) {
                    Spacer(modifier = GlanceModifier.defaultWeight())
                    Text(
                        text = content.weightEmpty,
                        maxLines = 3,
                        style = TextStyle(color = palette.muted, fontSize = 12.sp)
                    )
                    return@Column
                }
                Spacer(modifier = GlanceModifier.height(height = 4.dp))
                WidgetText(text = content.weightNumber, color = palette.ink, size = 26.sp, isNumber = true)
                WidgetText(text = content.weightChange, color = palette.accent, size = 11.sp, isBold = true)
                Box(modifier = GlanceModifier.fillMaxWidth().defaultWeight().padding(vertical = 4.dp)) {
                    if (weight.line.size >= 2) {
                        val (line, area) = WidgetBitmaps.sparkline(context = context, values = weight.line, widthDp = 140, heightDp = 40)
                        Image(
                            provider = ImageProvider(bitmap = area),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorProvider = palette.tonal),
                            modifier = GlanceModifier.fillMaxSize()
                        )
                        Image(
                            provider = ImageProvider(bitmap = line),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(colorProvider = palette.accent),
                            modifier = GlanceModifier.fillMaxSize()
                        )
                    }
                }
                WidgetText(text = content.weightCaption, color = palette.muted, size = 10.sp)
            }
        }
    }
}

/** Days in a row with food logged, and this week at a glance. */
internal class StreakWidget : SnapshotWidget() {

    @Composable
    override fun Content(content: WidgetContent, prefs: Preferences?) {
        val palette = widgetPalette(colours = prefs.colours())
        WidgetFrame(palette = palette, padding = 14.dp) {
            Column(modifier = GlanceModifier.fillMaxSize()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        provider = ImageProvider(resId = R.drawable.ic_widget_flame),
                        contentDescription = null,
                        colorFilter = ColorFilter.tint(colorProvider = palette.warm),
                        modifier = GlanceModifier.size(size = 14.dp)
                    )
                    Spacer(modifier = GlanceModifier.width(width = 6.dp))
                    WidgetText(text = content.streakTitle, color = palette.warm, size = 13.sp, isBold = true)
                }
                Row(verticalAlignment = Alignment.Bottom) {
                    WidgetText(text = content.streakDays, color = palette.ink, size = 36.sp, isNumber = true)
                    Spacer(modifier = GlanceModifier.width(width = 6.dp))
                    WidgetText(text = content.streakUnit, color = palette.muted, size = 12.sp, isBold = true, modifier = GlanceModifier.padding(bottom = 6.dp))
                }
                Spacer(modifier = GlanceModifier.defaultWeight())
                Row(modifier = GlanceModifier.fillMaxWidth()) {
                    content.snapshot.streak.week.zip(content.dayLetters).forEach { (day, letter) ->
                        Column(modifier = GlanceModifier.defaultWeight(), horizontalAlignment = Alignment.CenterHorizontally) {
                            Image(
                                provider = ImageProvider(resId = if (day.state == StreakDayState.OPEN) R.drawable.ic_widget_ring else R.drawable.ic_widget_dot),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(
                                    colorProvider = if (day.state == StreakDayState.LOGGED || day.state == StreakDayState.OPEN) palette.warm else palette.track
                                ),
                                modifier = GlanceModifier.size(size = 12.dp)
                            )
                            WidgetText(text = letter, color = palette.muted, size = 9.sp, isBold = true, align = TextAlign.Center)
                        }
                    }
                }
                Spacer(modifier = GlanceModifier.height(height = 4.dp))
                WidgetText(text = content.streakNote, color = palette.muted, size = 10.sp)
            }
        }
    }
}

/** Four round buttons that open the app straight to logging. */
internal class QuickLogWidget : SnapshotWidget() {

    @Composable
    override fun Content(content: WidgetContent, prefs: Preferences?) {
        val palette = widgetPalette(colours = prefs.colours())
        val context = LocalContext.current
        val buttons = listOf(
            QuickButton(content.logFood, R.drawable.ic_widget_add, palette.accent, palette.onAccent, AppShortcut.LOG_FOOD),
            QuickButton(content.scan, R.drawable.ic_widget_scan, palette.tonal, palette.tonalInk, AppShortcut.SCAN_BARCODE),
            QuickButton(content.waterTitle, R.drawable.ic_widget_water, palette.waterSoft, palette.water, AppShortcut.ADD_WATER),
            QuickButton(content.weightTitle, R.drawable.ic_widget_weight, palette.tonal, palette.tonalInk, AppShortcut.LOG_WEIGHT)
        )
        WidgetFrame(palette = palette, padding = 6.dp) {
            Row(modifier = GlanceModifier.fillMaxSize(), verticalAlignment = Alignment.CenterVertically) {
                buttons.forEach { button ->
                    Column(
                        modifier = GlanceModifier
                            .defaultWeight()
                            .fillMaxHeight()
                            .cornerRadius(radius = 16.dp)
                            .clickable(onClick = WidgetActions.shortcut(context = context, shortcut = button.shortcut)),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = GlanceModifier.size(size = 36.dp).background(colorProvider = button.background).cornerRadius(radius = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                provider = ImageProvider(resId = button.icon),
                                contentDescription = null,
                                colorFilter = ColorFilter.tint(colorProvider = button.content),
                                modifier = GlanceModifier.size(size = 18.dp)
                            )
                        }
                        Spacer(modifier = GlanceModifier.height(height = 2.dp))
                        WidgetText(text = button.label, color = palette.ink, size = 10.sp, isBold = true)
                    }
                }
            }
        }
    }

    private data class QuickButton(
        val label: String,
        val icon: Int,
        val background: ColorProvider,
        val content: ColorProvider,
        val shortcut: AppShortcut
    )
}

class TodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = TodayWidget()
}

class WaterWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WaterWidget()
}

class WeightWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = WeightWidget()
}

class StreakWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = StreakWidget()
}

class QuickLogWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = QuickLogWidget()
}
