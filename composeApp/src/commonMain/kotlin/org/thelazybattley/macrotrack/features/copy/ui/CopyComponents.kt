package org.thelazybattley.macrotrack.features.copy.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.close
import macrotrack.composeapp.generated.resources.copy_amount_quick_add
import macrotrack.composeapp.generated.resources.copy_amount_recipe
import macrotrack.composeapp.generated.resources.ic_check
import macrotrack.composeapp.generated.resources.ic_close
import macrotrack.composeapp.generated.resources.ic_minus
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FoodLog
import org.thelazybattley.macrotrack.ui.common.formatPortion
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** The copy screens' header: a close button, the title and what's being copied. */
@Composable
internal fun CopyHeader(title: String, subtitle: String, onClose: () -> Unit) {
    val shape = RoundedCornerShape(size = 14.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(start = 20.dp, end = 20.dp, top = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 44.dp)
                .clip(shape = shape)
                .background(color = colors.surface)
                .border(width = 1.dp, color = colors.line, shape = shape)
                .clickable(onClick = onClose),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(resource = Res.drawable.ic_close),
                contentDescription = stringResource(resource = Res.string.close),
                tint = colors.ink,
                modifier = Modifier.size(size = 18.dp)
            )
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Text(text = title, style = typography.bold24, color = colors.ink)
            Text(text = subtitle, style = typography.regular11, color = colors.muted)
        }
    }
}

enum class CheckState { ON, OFF, PARTIAL }

@Composable
internal fun CopyCheckBox(state: CheckState) {
    val isFilled = state != CheckState.OFF
    Box(
        modifier = Modifier
            .size(size = 22.dp)
            .clip(shape = RoundedCornerShape(size = 7.dp))
            .background(color = if (isFilled) colors.accent else colors.surface)
            .then(
                if (isFilled) Modifier else Modifier.border(width = 2.dp, color = colors.dashed, shape = RoundedCornerShape(size = 7.dp))
            ),
        contentAlignment = Alignment.Center
    ) {
        if (isFilled) {
            Icon(
                painter = painterResource(resource = if (state == CheckState.ON) Res.drawable.ic_check else Res.drawable.ic_minus),
                contentDescription = null,
                tint = colors.onAccent,
                modifier = Modifier.size(size = 13.dp)
            )
        }
    }
}

@Composable
internal fun CopySectionLabel(modifier: Modifier = Modifier, text: String) {
    Text(modifier = modifier, text = text, style = typography.bold12, color = colors.muted)
}

/** A logged portion as on the food log: "200 g", or Recipe or Quick add when there's no amount to show. */
@Composable
internal fun FoodLog.amountText(foodUnit: Map<String, String>): String = when {
    isQuickAdd -> stringResource(resource = Res.string.copy_amount_quick_add)
    isRecipe -> stringResource(resource = Res.string.copy_amount_recipe)
    else -> formatPortion(amount = weight, storedUnit = foodUnit[foodName].orEmpty())
}
