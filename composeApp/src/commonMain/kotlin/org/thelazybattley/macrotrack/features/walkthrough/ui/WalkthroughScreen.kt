package org.thelazybattley.macrotrack.features.walkthrough.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.breakfast
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.ic_flame
import macrotrack.composeapp.generated.resources.ic_meal_breakfast
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_lock
import macrotrack.composeapp.generated.resources.ic_star
import macrotrack.composeapp.generated.resources.ic_star_filled
import macrotrack.composeapp.generated.resources.kg
import macrotrack.composeapp.generated.resources.pro_feature_measurements
import macrotrack.composeapp.generated.resources.pro_name
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.remaining_today
import macrotrack.composeapp.generated.resources.walkthrough_day_streak
import macrotrack.composeapp.generated.resources.walkthrough_get_started
import macrotrack.composeapp.generated.resources.walkthrough_next
import macrotrack.composeapp.generated.resources.walkthrough_offline_note
import macrotrack.composeapp.generated.resources.walkthrough_page_of
import macrotrack.composeapp.generated.resources.walkthrough_pro_day_one
import macrotrack.composeapp.generated.resources.walkthrough_pro_feature_compare
import macrotrack.composeapp.generated.resources.walkthrough_pro_feature_insights
import macrotrack.composeapp.generated.resources.walkthrough_pro_feature_trends
import macrotrack.composeapp.generated.resources.walkthrough_pro_free_for
import macrotrack.composeapp.generated.resources.walkthrough_pro_note
import macrotrack.composeapp.generated.resources.walkthrough_pro_trial_ends
import macrotrack.composeapp.generated.resources.walkthrough_sample_food_1
import macrotrack.composeapp.generated.resources.walkthrough_sample_food_2
import macrotrack.composeapp.generated.resources.walkthrough_sample_food_3
import macrotrack.composeapp.generated.resources.walkthrough_skip
import macrotrack.composeapp.generated.resources.walkthrough_this_week
import macrotrack.composeapp.generated.resources.walkthrough_trend
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.PRO_TRIAL_DAYS
import org.thelazybattley.macrotrack.ui.common.CommonPrimaryButton
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun WalkthroughScreen(
    modifier: Modifier = Modifier,
    onFinished: () -> Unit
) {
    val pages = WalkthroughPage.entries
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()
    val isLastPage = pagerState.currentPage == pages.lastIndex

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(color = colors.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 56.dp)
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterEnd
        ) {
            if (!isLastPage) {
                Text(
                    text = stringResource(resource = Res.string.walkthrough_skip),
                    style = typography.bold15,
                    color = colors.muted,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .clickable(onClick = onFinished)
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                )
            }
        }
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth()
        ) { index ->
            WalkthroughPageContent(page = pages[index])
        }
        PageIndicator(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .padding(vertical = 16.dp),
            pageCount = pages.size,
            currentPage = pagerState.currentPage
        )
        CommonPrimaryButton(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 20.dp, end = 20.dp, bottom = 16.dp),
            text = if (isLastPage) Res.string.walkthrough_get_started else Res.string.walkthrough_next,
            onClick = {
                if (isLastPage) {
                    onFinished()
                } else {
                    scope.launch { pagerState.animateScrollToPage(page = pagerState.currentPage + 1) }
                }
            }
        )
    }
}

@Composable
private fun WalkthroughPageContent(page: WalkthroughPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(space = 24.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(weight = 1f)
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 28.dp))
                .background(color = colors.hero)
                .clearAndSetSemantics {}
                .padding(all = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(modifier = Modifier.widthIn(max = 340.dp)) {
                when (page) {
                    WalkthroughPage.WELCOME -> RemainingIllustration()
                    WalkthroughPage.LOG -> MealIllustration()
                    WalkthroughPage.INSIGHTS -> InsightsIllustration()
                    WalkthroughPage.PROGRESS -> WeightTrendIllustration()
                    WalkthroughPage.PRO -> ProTrialIllustration()
                }
            }
        }
        // Fixed height keeps the illustration the same size whether the title wraps or not.
        Column(
            modifier = Modifier.heightIn(min = 168.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            // Only the Pro page has a placeholder; extra format args are ignored by the other strings.
            Text(text = stringResource(page.titleRes, PRO_TRIAL_DAYS), style = typography.bold32, color = colors.ink)
            Text(text = stringResource(page.descriptionRes, PRO_TRIAL_DAYS), style = typography.regular13, color = colors.muted)
            val note = when (page) {
                WalkthroughPage.WELCOME -> Res.string.walkthrough_offline_note
                WalkthroughPage.PRO -> Res.string.walkthrough_pro_note
                else -> null
            }
            if (note != null) {
                Text(
                    text = stringResource(resource = note),
                    style = typography.bold12,
                    color = colors.accentInk,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun PageIndicator(
    modifier: Modifier = Modifier,
    pageCount: Int,
    currentPage: Int
) {
    val description = stringResource(resource = Res.string.walkthrough_page_of, currentPage + 1, pageCount)
    Row(
        modifier = modifier.semantics { contentDescription = description },
        horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        repeat(times = pageCount) { index ->
            val isSelected = index == currentPage
            val width by animateDpAsState(targetValue = if (isSelected) 24.dp else 8.dp)
            val color by animateColorAsState(targetValue = if (isSelected) colors.accent else colors.line)
            Box(
                modifier = Modifier
                    .width(width = width)
                    .height(height = 8.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun IllustrationCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(size = 20.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 20.dp))
            .padding(all = 16.dp)
    ) {
        content()
    }
}

@Composable
private fun RemainingIllustration() {
    IllustrationCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            val track = colors.heroTrack
            val accent = colors.accent
            Canvas(modifier = Modifier.size(size = 64.dp)) {
                val stroke = 7.dp.toPx()
                val inset = stroke / 2
                val arcSize = Size(width = size.width - stroke, height = size.height - stroke)
                drawArc(
                    color = track, startAngle = 0f, sweepAngle = 360f, useCenter = false,
                    topLeft = Offset(x = inset, y = inset), size = arcSize, style = Stroke(width = stroke)
                )
                drawArc(
                    color = accent, startAngle = -90f, sweepAngle = 230f, useCenter = false,
                    topLeft = Offset(x = inset, y = inset), size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round)
                )
            }
            Column {
                Text(text = stringResource(resource = Res.string.remaining_today), style = typography.regular11, color = colors.muted)
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                    Text(text = "1,165", style = typography.bold32, color = colors.ink)
                    Text(
                        text = "kcal",
                        style = typography.regular13,
                        color = colors.muted,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
            }
        }
        Spacer(modifier = Modifier.height(height = 14.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            MacroChip(modifier = Modifier.weight(weight = 1f), label = Res.string.protein, value = "29g", color = colors.protein, progress = 0.82f)
            MacroChip(modifier = Modifier.weight(weight = 1f), label = Res.string.carbs, value = "220g", color = colors.carbs, progress = 0.35f)
            MacroChip(modifier = Modifier.weight(weight = 1f), label = Res.string.fat, value = "18g", color = colors.fat, progress = 0.7f)
        }
    }
}

@Composable
private fun MacroChip(
    modifier: Modifier = Modifier,
    label: StringResource,
    value: String,
    color: Color,
    progress: Float
) {
    Column(
        modifier = modifier
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.background)
            .padding(all = 10.dp),
        verticalArrangement = Arrangement.spacedBy(space = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 5.dp)) {
            Box(modifier = Modifier.size(size = 7.dp).background(color = color, shape = RoundedCornerShape(size = 2.dp)))
            Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
        }
        Text(text = value, style = typography.bold15, color = colors.ink)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 4.dp)
                .background(color = colors.line, shape = CircleShape)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction = progress)
                    .height(height = 4.dp)
                    .background(color = color, shape = CircleShape)
            )
        }
    }
}

@Composable
private fun MealIllustration() {
    IllustrationCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_meal_breakfast),
                    contentDescription = null,
                    tint = colors.warmInk,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
            Text(
                text = stringResource(resource = Res.string.breakfast),
                style = typography.bold16,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(text = "504 kcal", style = typography.bold15, color = colors.ink)
        }
        Spacer(modifier = Modifier.height(height = 12.dp))
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        FoodRow(name = Res.string.walkthrough_sample_food_1, portion = "200 g", kcal = "194", isFavorite = true)
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        FoodRow(name = Res.string.walkthrough_sample_food_2, portion = "70 g", kcal = "265", isFavorite = false)
        HorizontalDivider(thickness = 1.dp, color = colors.divider)
        FoodRow(name = Res.string.walkthrough_sample_food_3, portion = "1 serving", kcal = "621", isFavorite = false)
    }
}

@Composable
private fun FoodRow(
    name: StringResource,
    portion: String,
    kcal: String,
    isFavorite: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(weight = 1f), verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
                Text(text = stringResource(resource = name), style = typography.bold13, color = colors.ink)
                if (isFavorite) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_star_filled),
                        contentDescription = null,
                        tint = colors.carbs,
                        modifier = Modifier.size(size = 14.dp)
                    )
                }
            }
            Text(text = portion, style = typography.regular11, color = colors.muted)
        }
        Text(text = "$kcal kcal", style = typography.bold13, color = colors.ink)
    }
}

@Composable
private fun InsightsIllustration() {
    val bars = listOf(0.78f, 0.92f, 0.85f, 1.08f, 0.88f, 0.95f, 0.9f)
    val days = listOf("M", "T", "W", "T", "F", "S", "S")
    Column(verticalArrangement = Arrangement.spacedBy(space = 12.dp)) {
        Row(
            modifier = Modifier
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_flame),
                contentDescription = null,
                tint = colors.fat,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(text = "12", style = typography.bold18, color = colors.ink)
            Text(text = stringResource(resource = Res.string.walkthrough_day_streak), style = typography.regular13, color = colors.muted)
        }
        IllustrationCard {
            Text(text = stringResource(resource = Res.string.walkthrough_this_week), style = typography.regular11, color = colors.muted)
            Text(text = "1,842 kcal/day", style = typography.bold18, color = colors.ink)
            Spacer(modifier = Modifier.height(height = 12.dp))
            val accent = colors.accent
            val over = colors.fat
            val track = colors.accentSoft
            val goalLine = colors.faint
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 110.dp)
            ) {
                val maxValue = 1.2f
                val slot = size.width / bars.size
                val barWidth = slot * 0.5f
                bars.forEachIndexed { index, value ->
                    val left = slot * index + (slot - barWidth) / 2
                    drawRoundRect(
                        color = track,
                        topLeft = Offset(x = left, y = 0f),
                        size = Size(width = barWidth, height = size.height),
                        cornerRadius = CornerRadius(x = 6.dp.toPx())
                    )
                    val barHeight = size.height * (value / maxValue)
                    drawRoundRect(
                        color = if (value > 1f) over else accent,
                        topLeft = Offset(x = left, y = size.height - barHeight),
                        size = Size(width = barWidth, height = barHeight),
                        cornerRadius = CornerRadius(x = 6.dp.toPx())
                    )
                }
                val goalY = size.height * (1 - 1f / maxValue)
                drawLine(
                    color = goalLine,
                    start = Offset(x = 0f, y = goalY),
                    end = Offset(x = size.width, y = goalY),
                    strokeWidth = 1.5.dp.toPx(),
                    pathEffect = PathEffect.dashPathEffect(intervals = floatArrayOf(8f, 8f))
                )
            }
            Spacer(modifier = Modifier.height(height = 6.dp))
            Row {
                days.forEach { day ->
                    Text(
                        text = day,
                        style = typography.regular11,
                        color = colors.muted,
                        modifier = Modifier.weight(weight = 1f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun WeightTrendIllustration() {
    val daily = listOf(70.4f, 70.9f, 70.2f, 70.6f, 69.9f, 70.3f, 69.6f, 69.9f, 69.3f, 69.7f, 69.1f, 69.4f, 68.9f, 69.0f)
    val average = daily.indices.map { index ->
        val window = daily.subList(fromIndex = (index - 6).coerceAtLeast(minimumValue = 0), toIndex = index + 1)
        window.average().toFloat()
    }
    IllustrationCard {
        Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(space = 4.dp)) {
            Text(text = "69.0", style = typography.bold32, color = colors.ink)
            Text(
                text = stringResource(resource = Res.string.kg),
                style = typography.regular13,
                color = colors.muted,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
        Text(text = "−1.4 kg", style = typography.bold12, color = colors.accentInk)
        Spacer(modifier = Modifier.height(height = 14.dp))
        val dot = colors.faint
        val accent = colors.accent
        val fill = colors.accentSoft
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 120.dp)
        ) {
            val minValue = daily.min() - 0.3f
            val maxValue = daily.max() + 0.3f
            val stepX = size.width / (daily.size - 1)
            fun point(index: Int, value: Float) = Offset(
                x = stepX * index,
                y = size.height * (1 - (value - minValue) / (maxValue - minValue))
            )

            val line = Path().apply {
                average.forEachIndexed { index, value ->
                    val p = point(index = index, value = value)
                    if (index == 0) moveTo(x = p.x, y = p.y) else lineTo(x = p.x, y = p.y)
                }
            }
            val area = Path().apply {
                addPath(path = line)
                lineTo(x = size.width, y = size.height)
                lineTo(x = 0f, y = size.height)
                close()
            }
            drawPath(path = area, color = fill)
            daily.forEachIndexed { index, value ->
                drawCircle(color = dot, radius = 3.dp.toPx(), center = point(index = index, value = value))
            }
            drawPath(path = line, color = accent, style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round))
        }
        Spacer(modifier = Modifier.height(height = 10.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            Box(modifier = Modifier.width(width = 14.dp).height(height = 3.dp).background(color = colors.accent, shape = CircleShape))
            Text(text = stringResource(resource = Res.string.walkthrough_trend), style = typography.regular11, color = colors.muted)
        }
    }
}

@Composable
private fun ProTrialIllustration() {
    IllustrationCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 40.dp)
                    .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_star),
                    contentDescription = null,
                    tint = colors.accentInk,
                    modifier = Modifier.size(size = 20.dp)
                )
            }
            Text(
                text = stringResource(resource = Res.string.pro_name),
                style = typography.bold16,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
            Text(
                text = stringResource(Res.string.walkthrough_pro_free_for, PRO_TRIAL_DAYS),
                style = typography.bold11,
                color = colors.warmInk,
                modifier = Modifier
                    .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        Spacer(modifier = Modifier.height(height = 14.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(weight = 1f)
                    .height(height = 8.dp)
                    .background(color = colors.accent, shape = CircleShape)
            )
            Icon(
                painter = painterResource(resource = Res.drawable.ic_lock),
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(size = 16.dp)
            )
        }
        Spacer(modifier = Modifier.height(height = 6.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(text = stringResource(resource = Res.string.walkthrough_pro_day_one), style = typography.regular11, color = colors.muted)
            Text(
                text = stringResource(Res.string.walkthrough_pro_trial_ends, PRO_TRIAL_DAYS),
                style = typography.regular11,
                color = colors.muted
            )
        }
        Spacer(modifier = Modifier.height(height = 14.dp))
        Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
            listOf(
                Res.string.walkthrough_pro_feature_trends,
                Res.string.walkthrough_pro_feature_compare,
                Res.string.pro_feature_measurements,
                Res.string.walkthrough_pro_feature_insights
            ).forEach { feature ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_check),
                        contentDescription = null,
                        tint = colors.accent,
                        modifier = Modifier.size(size = 16.dp)
                    )
                    Text(text = stringResource(resource = feature), style = typography.bold13, color = colors.ink)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewWalkthroughScreen() {
    MacroTrackTheme {
        WalkthroughScreen(onFinished = {})
    }
}
