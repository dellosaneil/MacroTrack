package org.thelazybattley.macrotrack.features.widgets.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.widget_kcal_left
import macrotrack.composeapp.generated.resources.widget_kcal_over
import macrotrack.composeapp.generated.resources.widget_macro_value
import macrotrack.composeapp.generated.resources.widget_today
import macrotrack.composeapp.generated.resources.widgets_blurb
import macrotrack.composeapp.generated.resources.widgets_footer
import macrotrack.composeapp.generated.resources.widgets_privacy
import macrotrack.composeapp.generated.resources.widgets_step_1
import macrotrack.composeapp.generated.resources.widgets_step_2
import macrotrack.composeapp.generated.resources.widgets_step_3
import macrotrack.composeapp.generated.resources.widgets_title
import macrotrack.composeapp.generated.resources.widgets_weight_sub
import macrotrack.composeapp.generated.resources.widgets_weight_title
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.domain.model.WidgetAmount
import org.thelazybattley.macrotrack.domain.model.WidgetSnapshot
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.widgets.WidgetsCallbacks
import org.thelazybattley.macrotrack.features.widgets.WidgetsViewModel
import org.thelazybattley.macrotrack.features.widgets.WidgetsViewState
import org.thelazybattley.macrotrack.ui.common.CommonScreenHeader
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography
import kotlin.math.abs

private val STEPS = listOf(Res.string.widgets_step_1, Res.string.widgets_step_2, Res.string.widgets_step_3)

@Composable
fun WidgetsScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val viewModel = koinViewModel<WidgetsViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    WidgetsScreen(modifier = modifier, viewState = viewState, callbacks = viewModel, onBack = onBack)
}

/** How to add the widgets, a preview of today's, and whether they show your weight. */
@Composable
private fun WidgetsScreen(
    modifier: Modifier = Modifier,
    viewState: WidgetsViewState,
    callbacks: WidgetsCallbacks,
    onBack: () -> Unit
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = colors.background,
        topBar = { CommonScreenHeader(title = Res.string.widgets_title, onBack = onBack) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(paddingValues = innerPadding)
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 16.dp)
        ) {
            viewState.snapshot?.let { PreviewStage(snapshot = it) }
            Text(text = stringResource(resource = Res.string.widgets_blurb), style = typography.regular13, color = colors.muted)
            Steps()
            Text(text = stringResource(resource = Res.string.widgets_privacy), style = typography.bold12, color = colors.muted)
            WeightRow(isOn = viewState.snapshot?.isWeightShown ?: true, onToggle = callbacks::onWeightShownToggled)
            Text(text = stringResource(resource = Res.string.widgets_footer), style = typography.regular11, color = colors.muted)
        }
    }
}

/** Today's widget as it looks now, on a soft wallpaper. */
@Composable
private fun PreviewStage(snapshot: WidgetSnapshot) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 24.dp))
            .background(color = colors.accentSoft)
            .padding(all = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 28.dp))
                .background(color = colors.surface)
                .padding(all = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(space = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CaloriesRing(calories = snapshot.calories)
            Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.widget_today), style = typography.bold12, color = colors.ink)
                MacroBar(name = Res.string.protein, amount = snapshot.protein, color = colors.protein)
                MacroBar(name = Res.string.carbs, amount = snapshot.carbs, color = colors.carbs)
                MacroBar(name = Res.string.fat, amount = snapshot.fat, color = colors.fat)
            }
        }
    }
}

@Composable
private fun CaloriesRing(calories: WidgetAmount) {
    val track = colors.line
    val accent = colors.accent
    Box(modifier = Modifier.size(size = 92.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val stroke = Stroke(width = 9.dp.toPx(), cap = StrokeCap.Round)
            drawArc(color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false, style = stroke)
            drawArc(color = accent, startAngle = -90f, sweepAngle = 360f * calories.progress, useCenter = false, style = stroke)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(text = abs(calories.left).withThousands(), style = typography.bold18, color = colors.ink)
            Text(
                text = stringResource(resource = if (calories.left < 0) Res.string.widget_kcal_over else Res.string.widget_kcal_left),
                style = typography.regular10,
                color = colors.muted
            )
        }
    }
}

@Composable
private fun MacroBar(name: StringResource, amount: WidgetAmount, color: Color) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 3.dp)) {
        Row {
            Text(text = stringResource(resource = name), style = typography.bold11, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            Text(
                text = stringResource(resource = Res.string.widget_macro_value, amount.eaten.withThousands(), amount.goal.withThousands()),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 6.dp)
                .clip(shape = CircleShape)
                .background(color = colors.line)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = amount.progress)
                    .fillMaxHeight()
                    .clip(shape = CircleShape)
                    .background(color = color)
            )
        }
    }
}

@Composable
private fun Steps() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        STEPS.forEachIndexed { index, step ->
            if (index > 0) HorizontalDivider(thickness = 1.dp, color = colors.divider)
            Row(
                modifier = Modifier.padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(size = 26.dp)
                        .clip(shape = CircleShape)
                        .background(color = colors.accentSoft),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = (index + 1).toString(), style = typography.bold13, color = colors.accentInk)
                }
                Text(text = stringResource(resource = step), style = typography.regular13, color = colors.ink, modifier = Modifier.weight(weight = 1f))
            }
        }
    }
}

@Composable
private fun WeightRow(isOn: Boolean, onToggle: () -> Unit) {
    val title = stringResource(resource = Res.string.widgets_weight_title)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 18.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 18.dp))
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = stringResource(resource = Res.string.widgets_weight_sub), style = typography.regular11, color = colors.muted)
        }
        CommonSwitch(isOn = isOn, description = title, onToggle = onToggle)
    }
}

@Preview
@Composable
private fun PreviewWidgetsScreen() {
    MacroTrackTheme {
        WidgetsScreen(viewState = WidgetsViewState(), callbacks = WidgetsCallbacks.default(), onBack = {})
    }
}
