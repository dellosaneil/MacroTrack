package org.thelazybattley.macrotrack.features.scanbarcode.ui

import androidx.compose.animation.core.LinearEasing
import org.thelazybattley.macrotrack.domain.model.PortionUnit
import org.thelazybattley.macrotrack.ui.common.portionUnit
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlin.math.roundToInt
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.base_serving
import macrotrack.composeapp.generated.resources.cancel
import macrotrack.composeapp.generated.resources.carbs
import macrotrack.composeapp.generated.resources.decrease_portion
import macrotrack.composeapp.generated.resources.fat
import macrotrack.composeapp.generated.resources.food_name
import macrotrack.composeapp.generated.resources.ic_camera
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_flashlight
import macrotrack.composeapp.generated.resources.ic_flashlight_on
import macrotrack.composeapp.generated.resources.ic_info
import macrotrack.composeapp.generated.resources.ic_keyboard
import macrotrack.composeapp.generated.resources.ic_package
import macrotrack.composeapp.generated.resources.ic_pencil
import macrotrack.composeapp.generated.resources.ic_plus
import macrotrack.composeapp.generated.resources.ic_search_off
import macrotrack.composeapp.generated.resources.increase_portion
import macrotrack.composeapp.generated.resources.kcal_text
import macrotrack.composeapp.generated.resources.portion_size
import macrotrack.composeapp.generated.resources.protein
import macrotrack.composeapp.generated.resources.scan_add_to_meal_log
import macrotrack.composeapp.generated.resources.scan_adding_to
import macrotrack.composeapp.generated.resources.scan_after_adding
import macrotrack.composeapp.generated.resources.scan_again
import macrotrack.composeapp.generated.resources.scan_allow_camera
import macrotrack.composeapp.generated.resources.scan_another
import macrotrack.composeapp.generated.resources.scan_barcode_number
import macrotrack.composeapp.generated.resources.scan_brand_barcode
import macrotrack.composeapp.generated.resources.scan_close
import macrotrack.composeapp.generated.resources.scan_create_with_barcode
import macrotrack.composeapp.generated.resources.scan_discard_scan_another
import macrotrack.composeapp.generated.resources.scan_edit_label
import macrotrack.composeapp.generated.resources.scan_flashlight
import macrotrack.composeapp.generated.resources.scan_found
import macrotrack.composeapp.generated.resources.scan_hint
import macrotrack.composeapp.generated.resources.scan_kcal_left_today
import macrotrack.composeapp.generated.resources.scan_label_serving
import macrotrack.composeapp.generated.resources.scan_later
import macrotrack.composeapp.generated.resources.scan_look_up
import macrotrack.composeapp.generated.resources.scan_looking_body
import macrotrack.composeapp.generated.resources.scan_looking_title
import macrotrack.composeapp.generated.resources.scan_manual_label
import macrotrack.composeapp.generated.resources.scan_manual_placeholder
import macrotrack.composeapp.generated.resources.scan_name_label
import macrotrack.composeapp.generated.resources.scan_not_found_body
import macrotrack.composeapp.generated.resources.scan_not_found_title
import macrotrack.composeapp.generated.resources.scan_not_saved_yet
import macrotrack.composeapp.generated.resources.scan_open_settings
import macrotrack.composeapp.generated.resources.scan_permission_body
import macrotrack.composeapp.generated.resources.scan_permission_title
import macrotrack.composeapp.generated.resources.scan_read
import macrotrack.composeapp.generated.resources.scan_save_and_add
import macrotrack.composeapp.generated.resources.scan_save_only
import macrotrack.composeapp.generated.resources.scan_saved
import macrotrack.composeapp.generated.resources.scan_search_by_name
import macrotrack.composeapp.generated.resources.scan_search_instead
import macrotrack.composeapp.generated.resources.scan_serving_amount
import macrotrack.composeapp.generated.resources.scan_serving_half
import macrotrack.composeapp.generated.resources.scan_subhint
import macrotrack.composeapp.generated.resources.scan_title
import macrotrack.composeapp.generated.resources.scan_type_number
import macrotrack.composeapp.generated.resources.scan_unavailable_body
import macrotrack.composeapp.generated.resources.scan_unavailable_title
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import org.thelazybattley.macrotrack.core.toCleanString
import org.thelazybattley.macrotrack.domain.model.FoodMacros
import org.thelazybattley.macrotrack.features.foodlog.ui.withThousands
import org.thelazybattley.macrotrack.features.scanbarcode.ScanBarcodeCallbacks
import org.thelazybattley.macrotrack.features.scanbarcode.ScanBarcodeViewModel
import org.thelazybattley.macrotrack.features.scanbarcode.ScanBarcodeViewState
import org.thelazybattley.macrotrack.features.scanbarcode.ScanMode
import org.thelazybattley.macrotrack.features.scanbarcode.ScannedFood
import org.thelazybattley.macrotrack.features.scanbarcode.ServingOption
import org.thelazybattley.macrotrack.features.scanbarcode.formatBarcode
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

// The camera view stays dark in both themes; the sheets on top of it follow the app theme.
private val CameraBackground = Color(color = 0xFF0D0E10)
private val OnCamera = Color.White
private val OnCameraMuted = Color.White.copy(alpha = 0.72f)
private val OnCameraButton = Color.White.copy(alpha = 0.14f)
private val ScanGreen = Color(color = 0xFF3FBF8F)
private val OnScanGreen = Color(color = 0xFF08241A)

@Composable
fun ScanBarcodeScreen(
    modifier: Modifier = Modifier,
    onBack: () -> Unit,
    onNavigate: (String) -> Unit,
    onSaved: () -> Unit
) {
    val viewModel = koinViewModel<ScanBarcodeViewModel>()
    val viewState by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(key1 = viewState.destinationRoute) {
        viewState.destinationRoute?.let { route ->
            onNavigate(route)
            viewModel.resetNavigateScreen()
        }
    }
    LaunchedEffect(key1 = viewState.isSaved) {
        if (viewState.isSaved) onSaved()
    }
    LaunchedEffect(key1 = viewState.isClosed) {
        if (viewState.isClosed) onBack()
    }
    ScanBarcodeScreen(
        modifier = modifier,
        viewState = viewState,
        callbacks = viewModel,
        onBack = onBack
    )
}

@Composable
private fun ScanBarcodeScreen(
    modifier: Modifier = Modifier,
    viewState: ScanBarcodeViewState,
    callbacks: ScanBarcodeCallbacks,
    onBack: () -> Unit
) {
    val permission = rememberCameraPermissionState()
    val showCamera = isCameraScanSupported && permission.isGranted
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(color = CameraBackground)
    ) {
        if (showCamera) {
            BarcodeCameraPreview(
                modifier = Modifier.fillMaxSize(),
                isTorchOn = viewState.isTorchOn,
                onBarcode = callbacks::onBarcodeDetected
            )
            ScanGuide(
                modifier = Modifier.align(alignment = Alignment.Center),
                mode = viewState.mode,
                barcode = viewState.barcode
            )
            CameraActions(
                modifier = Modifier.align(alignment = Alignment.BottomCenter),
                onTypeNumber = callbacks::onOpenManualEntry,
                onSearchByName = onBack
            )
        } else if (isCameraScanSupported) {
            CameraPermission(
                permission = permission,
                onTypeNumber = callbacks::onOpenManualEntry
            )
        }
        ScanTopBar(
            mealTitle = stringResource(resource = viewState.mealType.title),
            showTorch = showCamera,
            isTorchOn = viewState.isTorchOn,
            onClose = onBack,
            onToggleTorch = callbacks::onToggleTorch
        )
        if (viewState.isSheetOpen) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Black.copy(alpha = 0.5f))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        enabled = isCameraScanSupported,
                        onClick = callbacks::onScanAgain
                    )
            )
            ScanSheet(modifier = Modifier.align(alignment = Alignment.BottomCenter)) {
                when (viewState.mode) {
                    ScanMode.LOOKING_UP -> LookingUpContent(
                        barcode = viewState.barcode.orEmpty(),
                        onCancel = callbacks::onScanAgain
                    )
                    ScanMode.FOUND -> viewState.scanned?.let { scanned ->
                        FoundContent(
                            viewState = viewState,
                            scanned = scanned,
                            callbacks = callbacks
                        )
                    }
                    ScanMode.NOT_FOUND, ScanMode.UNAVAILABLE -> NotFoundContent(
                        barcode = viewState.barcode.orEmpty(),
                        isUnavailable = viewState.mode == ScanMode.UNAVAILABLE,
                        onCreateFood = callbacks::onCreateFood,
                        onScanAgain = callbacks::onScanAgain,
                        onSearchByName = onBack
                    )
                    ScanMode.MANUAL -> ManualEntryContent(
                        value = viewState.manualBarcode,
                        isValid = viewState.isManualBarcodeValid,
                        onValueChange = callbacks::onManualBarcodeChanged,
                        onLookUp = callbacks::onLookUpManualBarcode,
                        // Without a camera there's nothing to go back to but the food list.
                        onCancel = if (isCameraScanSupported) callbacks::onScanAgain else onBack
                    )
                    ScanMode.SCANNING -> Unit
                }
            }
        }
    }
}

@Composable
private fun ScanTopBar(
    mealTitle: String,
    showTorch: Boolean,
    isTorchOn: Boolean,
    onClose: () -> Unit,
    onToggleTorch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 16.dp, end = 16.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        RoundCameraButton(
            icon = Res.drawable.ic_close,
            contentDescription = stringResource(resource = Res.string.scan_close),
            isOn = false,
            onClick = onClose
        )
        Column(
            modifier = Modifier.weight(weight = 1f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = stringResource(resource = Res.string.scan_title), style = typography.bold18, color = OnCamera)
            Text(
                text = stringResource(Res.string.scan_adding_to, mealTitle),
                style = typography.regular11,
                color = OnCameraMuted
            )
        }
        if (showTorch) {
            RoundCameraButton(
                icon = if (isTorchOn) Res.drawable.ic_flashlight_on else Res.drawable.ic_flashlight,
                contentDescription = stringResource(resource = Res.string.scan_flashlight),
                isOn = isTorchOn,
                onClick = onToggleTorch
            )
        } else {
            Spacer(modifier = Modifier.size(size = 44.dp))
        }
    }
}

@Composable
private fun RoundCameraButton(
    icon: DrawableResource,
    contentDescription: String,
    isOn: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = CircleShape)
            .background(color = if (isOn) OnCamera else OnCameraButton)
            .clickable(role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = icon),
            contentDescription = null,
            tint = if (isOn) Color(color = 0xFF15171A) else OnCamera,
            modifier = Modifier.size(size = 20.dp)
        )
    }
}

/** The frame to line the barcode up in, with the hint under it. */
@Composable
private fun ScanGuide(
    modifier: Modifier = Modifier,
    mode: ScanMode,
    barcode: String?
) {
    val isScanning = mode == ScanMode.SCANNING
    Column(
        modifier = modifier.offset(y = (-20).dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(space = 20.dp)
    ) {
        Box(modifier = Modifier.size(width = 300.dp, height = 170.dp)) {
            val frameColor = if (isScanning) OnCamera else ScanGreen
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = 4.dp.toPx()
                val inset = stroke / 2
                val arm = 30.dp.toPx()
                val radius = 12.dp.toPx()
                val right = size.width - inset
                val bottom = size.height - inset
                val corners = Path().apply {
                    moveTo(x = inset, y = arm)
                    lineTo(x = inset, y = inset + radius)
                    arcTo(rect = Rect(left = inset, top = inset, right = inset + radius * 2, bottom = inset + radius * 2), startAngleDegrees = 180f, sweepAngleDegrees = 90f, forceMoveTo = false)
                    lineTo(x = arm, y = inset)
                    moveTo(x = right - arm, y = inset)
                    lineTo(x = right - radius, y = inset)
                    arcTo(rect = Rect(left = right - radius * 2, top = inset, right = right, bottom = inset + radius * 2), startAngleDegrees = 270f, sweepAngleDegrees = 90f, forceMoveTo = false)
                    lineTo(x = right, y = arm)
                    moveTo(x = right, y = bottom - arm)
                    lineTo(x = right, y = bottom - radius)
                    arcTo(rect = Rect(left = right - radius * 2, top = bottom - radius * 2, right = right, bottom = bottom), startAngleDegrees = 0f, sweepAngleDegrees = 90f, forceMoveTo = false)
                    lineTo(x = right - arm, y = bottom)
                    moveTo(x = arm, y = bottom)
                    lineTo(x = inset + radius, y = bottom)
                    arcTo(rect = Rect(left = inset, top = bottom - radius * 2, right = inset + radius * 2, bottom = bottom), startAngleDegrees = 90f, sweepAngleDegrees = 90f, forceMoveTo = false)
                    lineTo(x = inset, y = bottom - arm)
                }
                drawPath(path = corners, color = frameColor, style = Stroke(width = stroke, cap = StrokeCap.Round))
            }
            if (isScanning) {
                val transition = rememberInfiniteTransition()
                val progress by transition.animateFloat(
                    initialValue = 0f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(durationMillis = 1200, easing = LinearEasing),
                        repeatMode = RepeatMode.Reverse
                    )
                )
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val y = 12.dp.toPx() + (size.height - 24.dp.toPx()) * progress
                    val start = Offset(x = 18.dp.toPx(), y = y)
                    val end = Offset(x = size.width - 18.dp.toPx(), y = y)
                    drawLine(color = ScanGreen.copy(alpha = 0.3f), start = start, end = end, strokeWidth = 10.dp.toPx(), cap = StrokeCap.Round)
                    drawLine(color = ScanGreen, start = start, end = end, strokeWidth = 2.dp.toPx(), cap = StrokeCap.Round)
                }
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Text(
                text = stringResource(resource = if (isScanning) Res.string.scan_hint else Res.string.scan_read),
                style = typography.bold16,
                color = OnCamera,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (isScanning) {
                    stringResource(resource = Res.string.scan_subhint)
                } else {
                    barcode?.let(::formatBarcode).orEmpty()
                },
                style = typography.regular13,
                color = OnCameraMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CameraActions(
    modifier: Modifier = Modifier,
    onTypeNumber: () -> Unit,
    onSearchByName: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = 52.dp)
                .clip(shape = RoundedCornerShape(size = 16.dp))
                .background(color = Color.White.copy(alpha = 0.12f))
                .border(width = 1.dp, color = Color.White.copy(alpha = 0.22f), shape = RoundedCornerShape(size = 16.dp))
                .clickable(role = Role.Button, onClick = onTypeNumber),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_keyboard),
                contentDescription = null,
                tint = OnCamera,
                modifier = Modifier.size(size = 18.dp)
            )
            Text(text = stringResource(resource = Res.string.scan_type_number), style = typography.bold15, color = OnCamera)
        }
        Text(
            text = stringResource(resource = Res.string.scan_search_instead),
            style = typography.bold14,
            color = Color.White.copy(alpha = 0.85f),
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .clickable(role = Role.Button, onClick = onSearchByName)
                .padding(vertical = 13.dp)
        )
    }
}

@Composable
private fun CameraPermission(
    permission: CameraPermissionState,
    onTypeNumber: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .align(alignment = Alignment.Center)
                .offset(y = (-60).dp)
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(size = 72.dp)
                    .clip(shape = RoundedCornerShape(size = 22.dp))
                    .background(color = Color.White.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(resource = Res.drawable.ic_camera),
                    contentDescription = null,
                    tint = OnCamera,
                    modifier = Modifier.size(size = 34.dp)
                )
            }
            Text(
                text = stringResource(resource = Res.string.scan_permission_title),
                style = typography.bold24,
                color = OnCamera,
                textAlign = TextAlign.Center
            )
            Text(
                text = stringResource(resource = Res.string.scan_permission_body),
                style = typography.regular13,
                color = Color.White.copy(alpha = 0.78f),
                textAlign = TextAlign.Center
            )
        }
        Column(
            modifier = Modifier
                .align(alignment = Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Text(
                text = stringResource(
                    resource = if (permission.isPermanentlyDenied) Res.string.scan_open_settings else Res.string.scan_allow_camera
                ),
                style = typography.bold16,
                color = OnScanGreen,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .background(color = ScanGreen)
                    .clickable(role = Role.Button, onClick = permission.request)
                    .padding(vertical = 17.dp)
            )
            Text(
                text = stringResource(resource = Res.string.scan_type_number),
                style = typography.bold15,
                color = OnCamera,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 16.dp))
                    .clickable(role = Role.Button, onClick = onTypeNumber)
                    .padding(vertical = 14.dp)
            )
        }
    }
}

@Composable
private fun ScanSheet(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(color = colors.background)
            // Swallows taps so they don't reach the scrim behind the sheet.
            .pointerInput(key1 = Unit) { detectTapGestures {} }
            .navigationBarsPadding()
            .imePadding()
            .padding(start = 20.dp, end = 20.dp, top = 10.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Box(
            modifier = Modifier
                .align(alignment = Alignment.CenterHorizontally)
                .size(width = 40.dp, height = 5.dp)
                .background(color = colors.dashed, shape = CircleShape)
        )
        content()
    }
}

@Composable
private fun FoundContent(
    viewState: ScanBarcodeViewState,
    scanned: ScannedFood,
    callbacks: ScanBarcodeCallbacks
) {
    val food = scanned.food
    val isInMyFoods = scanned.isInMyFoods
    val barcode = food.barcode?.let(::formatBarcode) ?: viewState.barcode?.let(::formatBarcode).orEmpty()
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 56.dp)
                .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 16.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_package),
                contentDescription = null,
                tint = colors.warmInk,
                modifier = Modifier.size(size = 26.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            if (isInMyFoods) {
                Text(
                    text = food.name,
                    style = typography.bold18,
                    color = colors.ink,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            } else {
                FoodNameField(value = viewState.name, onValueChange = callbacks::onNameChanged)
                Spacer(modifier = Modifier.height(height = 4.dp))
            }
            Text(
                text = scanned.brand?.let { stringResource(Res.string.scan_brand_barcode, it, barcode) }
                    ?: stringResource(Res.string.scan_barcode_number, barcode),
                style = typography.regular11,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Row(
            modifier = Modifier
                .background(color = colors.accentSoft, shape = RoundedCornerShape(size = 8.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_check),
                contentDescription = null,
                tint = colors.accentInk,
                modifier = Modifier.size(size = 12.dp)
            )
            Text(
                text = stringResource(resource = if (isInMyFoods) Res.string.scan_saved else Res.string.scan_found),
                style = typography.bold12,
                color = colors.accentInk
            )
        }
    }

    if (!isInMyFoods) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(shape = RoundedCornerShape(size = 14.dp))
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 14.dp))
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_info),
                contentDescription = null,
                tint = colors.muted,
                modifier = Modifier.size(size = 16.dp)
            )
            Text(
                text = stringResource(resource = Res.string.scan_not_saved_yet),
                style = typography.regular11,
                color = colors.muted
            )
        }
    }

    // Grams show as oz and ml as fl oz when imperial; the amount itself stays in the food's unit.
    val portionUnit = portionUnit(storedUnit = food.unit)
    if (scanned.servings.size > 1) {
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            scanned.servings.forEach { serving ->
                ServingChip(
                    modifier = Modifier.weight(weight = 1f),
                    text = servingText(serving = serving, portionUnit = portionUnit),
                    isSelected = viewState.amount == serving.amount,
                    onClick = { callbacks.onAmountChanged(amount = serving.amount) }
                )
            }
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.portion_size), style = typography.bold14, color = colors.ink)
            Text(
                text = scanned.servingLabel?.let { stringResource(Res.string.scan_label_serving, it) }
                    ?: stringResource(Res.string.base_serving, portionUnit.format(amount = food.weight), portionUnit.label),
                style = typography.regular11,
                color = colors.muted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        val step = if (portionUnit.isConverted) OUNCE_STEP else viewState.amountStep
        val shownAmount = portionUnit.fromStored(amount = viewState.amount)
        StepperButton(
            label = "−",
            contentDescription = stringResource(resource = Res.string.decrease_portion),
            enabled = shownAmount - step > 0,
            onClick = { callbacks.onAmountChanged(amount = portionUnit.toStored(value = (shownAmount - step).roundToStep(step = step))) }
        )
        Row(
            modifier = Modifier
                .width(width = 84.dp)
                .height(height = 44.dp)
                .clip(shape = RoundedCornerShape(size = 12.dp))
                .background(color = colors.surface)
                .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 12.dp)),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 3.dp, alignment = Alignment.CenterHorizontally)
        ) {
            Text(text = portionUnit.format(amount = viewState.amount), style = typography.bold18, color = colors.ink)
            Text(text = portionUnit.label, style = typography.regular11, color = colors.muted)
        }
        StepperButton(
            label = "+",
            contentDescription = stringResource(resource = Res.string.increase_portion),
            enabled = true,
            onClick = { callbacks.onAmountChanged(amount = portionUnit.toStored(value = (shownAmount + step).roundToStep(step = step))) }
        )
    }

    viewState.portionMacros?.let { macros -> PortionMacros(macros = macros) }

    viewState.remainingAfterAdding?.let { remaining ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = colors.hero, shape = RoundedCornerShape(size = 14.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(Res.string.scan_after_adding, stringResource(resource = viewState.mealType.title)),
                style = typography.regular13,
                color = colors.muted
            )
            Text(
                text = stringResource(Res.string.scan_kcal_left_today, remaining.withThousands()),
                style = typography.bold13,
                color = colors.accentInk
            )
        }
    }

    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        val mealTitle = stringResource(resource = viewState.mealType.title)
        SheetButton(
            text = stringResource(resource = if (isInMyFoods) Res.string.scan_later else Res.string.scan_save_only),
            isPrimary = false,
            enabled = viewState.canSave,
            onClick = callbacks::onLater
        )
        SheetButton(
            modifier = Modifier.weight(weight = 1f),
            text = if (isInMyFoods) {
                stringResource(Res.string.scan_add_to_meal_log, mealTitle)
            } else {
                stringResource(Res.string.scan_save_and_add, mealTitle)
            },
            icon = Res.drawable.ic_plus,
            isPrimary = true,
            enabled = viewState.canSave,
            onClick = callbacks::onAddToMeal
        )
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(space = 16.dp, alignment = Alignment.CenterHorizontally)
    ) {
        TextLink(
            text = stringResource(resource = if (isInMyFoods) Res.string.scan_another else Res.string.scan_discard_scan_another),
            onClick = callbacks::onScanAgain
        )
        TextLink(text = stringResource(resource = Res.string.scan_edit_label), onClick = callbacks::onEditFood)
    }
}

/** The product's name from Open Food Facts, which the user can fix before it's saved to their foods. */
@Composable
private fun FoodNameField(value: String, onValueChange: (String) -> Unit) {
    val focusManager = LocalFocusManager.current
    val label = stringResource(resource = Res.string.food_name)
    Column(verticalArrangement = Arrangement.spacedBy(space = 2.dp)) {
        Text(text = stringResource(resource = Res.string.scan_name_label), style = typography.bold11, color = colors.muted)
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = typography.bold16.copy(color = colors.ink),
            cursorBrush = SolidColor(value = colors.accent),
            keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            modifier = Modifier
                .fillMaxWidth()
                .semantics { contentDescription = label },
            decorationBox = { innerTextField ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 40.dp)
                        .clip(shape = RoundedCornerShape(size = 12.dp))
                        .background(color = colors.surface)
                        .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 12.dp))
                        .padding(horizontal = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 6.dp)
                ) {
                    Box(modifier = Modifier.weight(weight = 1f)) { innerTextField() }
                    Icon(
                        painter = painterResource(resource = Res.drawable.ic_pencil),
                        contentDescription = null,
                        tint = colors.accentInk,
                        modifier = Modifier.size(size = 16.dp)
                    )
                }
            }
        )
    }
}

@Composable
private fun TextLink(text: String, onClick: () -> Unit) {
    Text(
        text = text,
        style = typography.bold12,
        color = colors.accentInk,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(size = 8.dp))
            .clickable(role = Role.Button, onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 9.dp)
    )
}

/** Shown while the barcode is looked up: the sheet's shape, with pulsing placeholders where the food will appear. */
@Composable
private fun LookingUpContent(
    barcode: String,
    onCancel: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(size = 28.dp),
            color = colors.accent,
            trackColor = colors.line,
            strokeWidth = 3.5.dp,
            strokeCap = StrokeCap.Round
        )
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = stringResource(resource = Res.string.scan_looking_title), style = typography.bold18, color = colors.ink)
            Text(
                text = stringResource(Res.string.scan_barcode_number, formatBarcode(digits = barcode)),
                style = typography.regular13,
                color = colors.muted
            )
        }
    }
    val pulse by rememberInfiniteTransition().animateFloat(
        initialValue = 1f,
        targetValue = 0.45f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700),
            repeatMode = RepeatMode.Reverse
        )
    )
    val placeholder = Modifier.graphicsLayer { alpha = pulse }
    Column(
        modifier = Modifier.clearAndSetSemantics {},
        verticalArrangement = Arrangement.spacedBy(space = 14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
        ) {
            Placeholder(modifier = placeholder.size(size = 56.dp), radius = 16)
            Column(
                modifier = Modifier.weight(weight = 1f),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                Placeholder(modifier = placeholder.fillMaxWidth(fraction = 0.75f).height(height = 16.dp), radius = 8)
                Placeholder(modifier = placeholder.fillMaxWidth(fraction = 0.5f).height(height = 12.dp), radius = 6)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            repeat(times = 3) {
                Placeholder(modifier = placeholder.weight(weight = 1f).height(height = 40.dp), radius = 12)
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
            repeat(times = 4) {
                Placeholder(modifier = placeholder.weight(weight = 1f).height(height = 54.dp), radius = 12)
            }
        }
    }
    Text(
        text = stringResource(resource = Res.string.scan_looking_body),
        style = typography.regular11,
        color = colors.muted,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    SheetButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(resource = Res.string.cancel),
        isPrimary = false,
        onClick = onCancel
    )
}

@Composable
private fun Placeholder(modifier: Modifier, radius: Int) {
    Box(modifier = modifier.background(color = colors.line, shape = RoundedCornerShape(size = radius.dp)))
}

@Composable
private fun servingText(serving: ServingOption, portionUnit: PortionUnit): String {
    val amount = portionUnit.format(amount = serving.amount)
    val unit = portionUnit.label
    return when {
        serving.label != null -> serving.label
        serving.isHalf -> stringResource(Res.string.scan_serving_half, amount, unit)
        else -> stringResource(Res.string.scan_serving_amount, amount, unit)
    }
}

@Composable
private fun ServingChip(
    modifier: Modifier = Modifier,
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .height(height = 40.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = if (isSelected) colors.accentSoft else colors.surface)
            .border(width = 1.dp, color = if (isSelected) colors.accent else colors.line, shape = RoundedCornerShape(size = 12.dp))
            .clickable(role = Role.RadioButton, onClick = onClick)
            .padding(horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = typography.bold13,
            color = if (isSelected) colors.accentInk else colors.ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun StepperButton(
    label: String,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 44.dp)
            .clip(shape = RoundedCornerShape(size = 12.dp))
            .background(color = colors.surface)
            .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Text(text = label, style = typography.bold18, color = if (enabled) colors.ink else colors.faint)
    }
}

@Composable
private fun PortionMacros(macros: FoodMacros) {
    Row(horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        listOf<Triple<StringResource, String, Color?>>(
            Triple(Res.string.kcal_text, macros.calories.withThousands(), null),
            Triple(Res.string.protein, "${macros.protein.roundToInt()}g", colors.protein),
            Triple(Res.string.carbs, "${macros.carbs.roundToInt()}g", colors.carbs),
            Triple(Res.string.fat, "${macros.fat.roundToInt()}g", colors.fat)
        ).forEach { (label, value, dot) ->
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .background(color = colors.surface)
                    .border(width = 1.dp, color = colors.line, shape = RoundedCornerShape(size = 12.dp))
                    .padding(horizontal = 10.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(space = 4.dp)
                ) {
                    dot?.let { Box(modifier = Modifier.size(size = 7.dp).background(color = it, shape = RoundedCornerShape(size = 2.dp))) }
                    Text(text = stringResource(resource = label), style = typography.regular11, color = colors.muted)
                }
                Text(text = value, style = typography.bold16, color = colors.ink)
            }
        }
    }
}

@Composable
private fun NotFoundContent(
    barcode: String,
    isUnavailable: Boolean,
    onCreateFood: () -> Unit,
    onScanAgain: () -> Unit,
    onSearchByName: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(size = 52.dp)
            .background(color = colors.warmSoft, shape = RoundedCornerShape(size = 16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(resource = Res.drawable.ic_search_off),
            contentDescription = null,
            tint = colors.warmInk,
            modifier = Modifier.size(size = 26.dp)
        )
    }
    Column(verticalArrangement = Arrangement.spacedBy(space = 4.dp)) {
        Text(
            text = stringResource(
                resource = if (isUnavailable) Res.string.scan_unavailable_title else Res.string.scan_not_found_title
            ),
            style = typography.bold24,
            color = colors.ink
        )
        val formatted = formatBarcode(digits = barcode)
        val body = stringResource(
            resource = if (isUnavailable) Res.string.scan_unavailable_body else Res.string.scan_not_found_body,
            formatted
        )
        Text(
            text = buildAnnotatedString {
                val start = body.indexOf(string = formatted)
                if (start < 0) {
                    append(body)
                } else {
                    append(body.substring(startIndex = 0, endIndex = start))
                    withStyle(style = SpanStyle(fontWeight = FontWeight.Bold, color = colors.ink)) { append(formatted) }
                    append(body.substring(startIndex = start + formatted.length))
                }
            },
            style = typography.regular13,
            color = colors.muted
        )
    }
    SheetButton(
        modifier = Modifier.fillMaxWidth(),
        text = stringResource(resource = Res.string.scan_create_with_barcode),
        icon = Res.drawable.ic_plus,
        isPrimary = true,
        onClick = onCreateFood
    )
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SheetButton(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.scan_again),
            isPrimary = false,
            height = 48,
            onClick = onScanAgain
        )
        SheetButton(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.scan_search_by_name),
            isPrimary = false,
            height = 48,
            onClick = onSearchByName
        )
    }
}

@Composable
private fun ManualEntryContent(
    value: String,
    isValid: Boolean,
    onValueChange: (String) -> Unit,
    onLookUp: () -> Unit,
    onCancel: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(key1 = Unit) { focusRequester.requestFocus() }
    Text(text = stringResource(resource = Res.string.scan_type_number), style = typography.bold24, color = colors.ink)
    Column(verticalArrangement = Arrangement.spacedBy(space = 6.dp)) {
        Text(text = stringResource(resource = Res.string.scan_manual_label), style = typography.bold12, color = colors.muted)
        val fieldStyle = typography.regular18.copy(
            color = colors.ink,
            fontFamily = FontFamily.Monospace,
            letterSpacing = 1.4.sp
        )
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            textStyle = fieldStyle,
            cursorBrush = SolidColor(value = colors.accent),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { if (isValid) onLookUp() }),
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester = focusRequester),
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(height = 54.dp)
                        .clip(shape = RoundedCornerShape(size = 14.dp))
                        .background(color = colors.surface)
                        .border(width = 1.5.dp, color = colors.accent, shape = RoundedCornerShape(size = 14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (value.isEmpty()) {
                        Text(
                            text = stringResource(resource = Res.string.scan_manual_placeholder),
                            style = fieldStyle.copy(color = colors.faint)
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
    Row(horizontalArrangement = Arrangement.spacedBy(space = 8.dp)) {
        SheetButton(
            text = stringResource(resource = Res.string.cancel),
            isPrimary = false,
            onClick = onCancel
        )
        SheetButton(
            modifier = Modifier.weight(weight = 1f),
            text = stringResource(resource = Res.string.scan_look_up),
            isPrimary = true,
            enabled = isValid,
            onClick = onLookUp
        )
    }
}

@Composable
private fun SheetButton(
    modifier: Modifier = Modifier,
    text: String,
    isPrimary: Boolean,
    icon: DrawableResource? = null,
    enabled: Boolean = true,
    height: Int = 52,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(size = if (height > 50) 16.dp else 14.dp)
    val contentColor = when {
        !isPrimary -> colors.ink
        enabled -> colors.onAccent
        else -> colors.faint
    }
    Row(
        modifier = modifier
            .height(height = height.dp)
            .clip(shape = shape)
            .background(
                color = when {
                    !isPrimary -> colors.surface
                    enabled -> colors.accent
                    else -> colors.line
                }
            )
            .then(if (isPrimary) Modifier else Modifier.border(width = 1.dp, color = colors.line, shape = shape))
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp, alignment = Alignment.CenterHorizontally)
    ) {
        icon?.let {
            Icon(
                painter = painterResource(resource = it),
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Text(
            text = text,
            style = if (isPrimary) typography.bold16 else typography.bold14,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/** Imperial portions of foods stored in grams or ml move in half ounces. */
private const val OUNCE_STEP = 0.5

/** Snaps to the step so ounce amounts converted from grams stay on whole half ounces. */
private fun Double.roundToStep(step: Double): Double = kotlin.math.round(this / step) * step
