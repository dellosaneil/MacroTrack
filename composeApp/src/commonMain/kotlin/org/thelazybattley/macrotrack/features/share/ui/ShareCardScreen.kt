package org.thelazybattley.macrotrack.features.share.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.layer.GraphicsLayer
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.export_share
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_download
import macrotrack.composeapp.generated.resources.ic_share_out
import macrotrack.composeapp.generated.resources.photo_before_after
import macrotrack.composeapp.generated.resources.share_color
import macrotrack.composeapp.generated.resources.share_footnote
import macrotrack.composeapp.generated.resources.share_kind_week
import macrotrack.composeapp.generated.resources.share_kind_weight
import macrotrack.composeapp.generated.resources.share_save
import macrotrack.composeapp.generated.resources.share_save_failed
import macrotrack.composeapp.generated.resources.share_saved
import macrotrack.composeapp.generated.resources.share_show_weight
import macrotrack.composeapp.generated.resources.share_show_weight_hint
import macrotrack.composeapp.generated.resources.share_show_weight_photos
import macrotrack.composeapp.generated.resources.share_size
import macrotrack.composeapp.generated.resources.share_square
import macrotrack.composeapp.generated.resources.share_story
import macrotrack.composeapp.generated.resources.share_style_dark
import macrotrack.composeapp.generated.resources.share_style_green
import macrotrack.composeapp.generated.resources.share_style_light
import macrotrack.composeapp.generated.resources.share_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.core.platform.SharedFile
import org.thelazybattley.macrotrack.core.platform.encodeToPng
import org.thelazybattley.macrotrack.core.platform.rememberFileActions
import org.thelazybattley.macrotrack.features.mealplan.ui.PlanHeader
import org.thelazybattley.macrotrack.features.photos.ui.PhotoSegmented
import org.thelazybattley.macrotrack.features.share.ShareCardCallbacks
import org.thelazybattley.macrotrack.features.share.ShareCardFormat
import org.thelazybattley.macrotrack.features.share.ShareCardKind
import org.thelazybattley.macrotrack.features.share.ShareCardStyle
import org.thelazybattley.macrotrack.features.share.ShareCardViewModel
import org.thelazybattley.macrotrack.features.share.ShareCardViewState
import org.thelazybattley.macrotrack.features.share.WeekCard
import org.thelazybattley.macrotrack.ui.common.CommonSwitch
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ShareCardScreen(onClose: () -> Unit) {
    val viewModel = koinViewModel<ShareCardViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    ShareCardScreen(viewState = viewState, callbacks = viewModel, onClose = onClose)
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ShareCardScreen(
    viewState: ShareCardViewState,
    callbacks: ShareCardCallbacks,
    onClose: () -> Unit
) {
    val fileActions = rememberFileActions()
    val scope = rememberCoroutineScope()
    val exportLayer = rememberGraphicsLayer()
    var isBusy by remember { mutableStateOf(value = false) }
    var notice by remember { mutableStateOf<StringResource?>(value = null) }
    LaunchedEffect(key1 = notice) {
        if (notice != null) {
            delay(timeMillis = NOTICE_MILLIS)
            notice = null
        }
    }
    val fileName = "fitmetric-" + viewState.kind.name.lowercase() + "-" + viewState.today + ".png"
    // Drawn on the phone from the full-size copy below, only when Save or Share is tapped.
    val export = { onFile: (SharedFile) -> Unit ->
        if (!isBusy) {
            isBusy = true
            scope.launch {
                val bytes = runCatching {
                    val bitmap = exportLayer.toImageBitmap()
                    withContext(context = Dispatchers.Default) { bitmap.encodeToPng() }
                }.getOrNull()
                isBusy = false
                if (bytes == null || bytes.isEmpty()) {
                    notice = Res.string.share_save_failed
                } else {
                    onFile(SharedFile(name = fileName, mimeType = PNG_MIME_TYPE, bytes = bytes))
                }
            }
        }
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = colors.background)
    ) {
        PlanHeader(
            leadingIcon = Res.drawable.ic_close,
            leadingDescription = stringResource(resource = Res.string.close),
            onLeading = onClose
        ) {
            Text(
                text = stringResource(resource = Res.string.share_title),
                style = typography.bold24,
                color = colors.ink,
                modifier = Modifier.weight(weight = 1f)
            )
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, top = 14.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            FlowRow(horizontalArrangement = Arrangement.spacedBy(space = 6.dp), verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                viewState.kinds.forEach { kind ->
                    KindChip(text = stringResource(resource = kind.label), isSelected = kind == viewState.kind) {
                        callbacks.onKindSelected(kind = kind)
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height = 492.dp)
                    .clip(shape = RoundedCornerShape(size = 24.dp))
                    .background(color = colors.line),
                contentAlignment = Alignment.Center
            ) {
                if (viewState.isLoaded) {
                    ExportCopy(viewState = viewState, layer = exportLayer)
                    ShareCard(
                        modifier = Modifier.shadow(elevation = 12.dp, shape = RoundedCornerShape(size = CARD_RADIUS)),
                        viewState = viewState
                    )
                }
            }
            Section(title = stringResource(resource = Res.string.share_size)) {
                PhotoSegmented(
                    options = ShareCardFormat.entries,
                    selected = viewState.format,
                    label = { stringResource(resource = if (it == ShareCardFormat.STORY) Res.string.share_story else Res.string.share_square) },
                    onSelect = callbacks::onFormatSelected
                )
            }
            Section(title = stringResource(resource = Res.string.share_color)) {
                Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                    ShareCardStyle.entries.forEach { style ->
                        StyleButton(
                            modifier = Modifier.weight(weight = 1f),
                            style = style,
                            isSelected = style == viewState.style,
                            onClick = { callbacks.onStyleSelected(style = style) }
                        )
                    }
                }
            }
            WeightOption(viewState = viewState, onToggle = callbacks::onWeightShownToggled)
            Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
                ActionButton(
                    modifier = Modifier.weight(weight = 1f),
                    icon = Res.drawable.ic_download,
                    text = stringResource(resource = Res.string.share_save),
                    isPrimary = false,
                    enabled = !isBusy,
                    onClick = {
                        export { file ->
                            fileActions.saveImage(file = file) { isSaved ->
                                notice = if (isSaved) Res.string.share_saved else Res.string.share_save_failed
                            }
                        }
                    }
                )
                ActionButton(
                    modifier = Modifier.weight(weight = 1.4f),
                    icon = Res.drawable.ic_share_out,
                    text = stringResource(resource = Res.string.export_share),
                    isPrimary = true,
                    enabled = !isBusy,
                    onClick = { export(fileActions::share) }
                )
            }
            Text(
                text = stringResource(resource = notice ?: Res.string.share_footnote),
                style = if (notice != null) typography.bold12 else typography.regular11,
                color = if (notice != null) colors.ink else colors.muted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * The card again at its full export size, [EXPORT_WIDTH_PX] wide: the same layout drawn at a larger density, recorded
 * into [layer] but never drawn on screen.
 */
@Composable
private fun ExportCopy(viewState: ShareCardViewState, layer: GraphicsLayer) {
    val frameWidth = viewState.format.frameWidth
    CompositionLocalProvider(LocalDensity provides Density(density = EXPORT_WIDTH_PX / frameWidth.value, fontScale = 1f)) {
        ShareCard(
            modifier = Modifier
                .clearAndSetSemantics {}
                .drawWithContent {
                    layer.record { this@drawWithContent.drawContent() }
                },
            viewState = viewState
        )
    }
}

@Composable
private fun KindChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 18.dp)
    Box(
        modifier = Modifier
            .height(height = 36.dp)
            .clip(shape = shape)
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics { selected = isSelected }
            .padding(horizontal = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, style = typography.bold13, color = if (isSelected) colors.accentInk else colors.ink)
    }
}

@Composable
private fun Section(title: String, content: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        Text(text = title, style = typography.bold12, color = colors.muted)
        content()
    }
}

@Composable
private fun StyleButton(modifier: Modifier = Modifier, style: ShareCardStyle, isSelected: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = modifier
            .height(height = 48.dp)
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = if (isSelected) 2.dp else 1.dp, color = if (isSelected) colors.accent else colors.line, shape = shape)
            .clickable(role = Role.RadioButton, onClick = onClick)
            .semantics(mergeDescendants = true) { selected = isSelected },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Box(
            modifier = Modifier
                .size(size = 18.dp)
                .background(color = style.palette.background, shape = CircleShape)
                .border(width = 1.dp, color = SWATCH_RING, shape = CircleShape)
        )
        Text(text = stringResource(resource = style.label), style = typography.bold13, color = colors.ink)
    }
}

@Composable
private fun WeightOption(viewState: ShareCardViewState, onToggle: () -> Unit) {
    val title = stringResource(
        resource = if (viewState.kind == ShareCardKind.PHOTOS) Res.string.share_show_weight_photos else Res.string.share_show_weight
    )
    val shape = RoundedCornerShape(size = 18.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape = shape)
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = shape)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold14, color = colors.ink)
            Text(text = stringResource(resource = Res.string.share_show_weight_hint), style = typography.regular11, color = colors.muted)
        }
        CommonSwitch(isOn = viewState.isWeightShown, description = title, onToggle = onToggle)
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    icon: DrawableResource,
    text: String,
    isPrimary: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = 14.dp)
    val content = if (isPrimary) colors.onAccent else colors.ink
    Row(
        modifier = modifier
            .height(height = 52.dp)
            .clip(shape = shape)
            .background(color = if (isPrimary) colors.accent else colors.surface)
            .then(if (isPrimary) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        Icon(painter = painterResource(resource = icon), contentDescription = null, tint = content, modifier = Modifier.size(size = 18.dp))
        Text(text = text, style = typography.bold14, color = content)
    }
}

private val ShareCardKind.label: StringResource
    get() = when (this) {
        ShareCardKind.WEEK -> Res.string.share_kind_week
        ShareCardKind.WEIGHT -> Res.string.share_kind_weight
        ShareCardKind.PHOTOS -> Res.string.photo_before_after
    }

private val ShareCardStyle.label: StringResource
    get() = when (this) {
        ShareCardStyle.LIGHT -> Res.string.share_style_light
        ShareCardStyle.DARK -> Res.string.share_style_dark
        ShareCardStyle.GREEN -> Res.string.share_style_green
    }

/** The width of the image saved or shared, in pixels; a story is 1080 × 1920 and a square 1080 × 1080. */
private const val EXPORT_WIDTH_PX = 1080f
private const val PNG_MIME_TYPE = "image/png"
private const val NOTICE_MILLIS = 3_000L
private val SWATCH_RING = Color(color = 0x737F7F7F)

@Preview(showBackground = true, backgroundColor = 0xFFF4F3EE)
@Composable
private fun PreviewShareCardScreen() {
    val today = getCurrentDate()
    MacroTrackTheme {
        ShareCardScreen(
            viewState = ShareCardViewState(
                week = WeekCard(
                    start = today,
                    end = today,
                    bars = emptyList(),
                    daysOnTarget = 5,
                    averageCalories = 2_440,
                    averageProtein = 131,
                    weightChangeKg = -0.3
                ),
                isLoaded = true
            ),
            callbacks = ShareCardCallbacks.default(),
            onClose = {}
        )
    }
}
