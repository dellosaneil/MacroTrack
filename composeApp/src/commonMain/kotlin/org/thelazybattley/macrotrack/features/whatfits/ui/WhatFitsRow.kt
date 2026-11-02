package org.thelazybattley.macrotrack.features.whatfits.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.ic_bulb
import macrotrack.composeapp.generated.resources.ic_chevron_right
import macrotrack.composeapp.generated.resources.what_fits
import macrotrack.composeapp.generated.resources.what_fits_open
import macrotrack.composeapp.generated.resources.what_fits_row_ideas
import macrotrack.composeapp.generated.resources.what_fits_row_new
import macrotrack.composeapp.generated.resources.what_fits_row_none
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.pluralStringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.FitFood
import org.thelazybattley.macrotrack.domain.model.FitMacros
import org.thelazybattley.macrotrack.domain.model.MealType
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings
import org.thelazybattley.macrotrack.domain.model.findFitIdeas
import org.thelazybattley.macrotrack.domain.model.fitMealOptions
import org.thelazybattley.macrotrack.features.pro.ui.ProBadge
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

/** How many ideas What fits? has for [left], for the next meal of the day; null when nothing has been logged before. */
fun whatFitsCount(
    foods: List<FitFood>,
    settings: WhatFitsSettings,
    left: FitMacros,
    loggedMeals: Set<MealType>,
    eatenToday: Set<String>
): Int? {
    if (foods.isEmpty()) return null
    return findFitIdeas(
        foods = foods,
        left = left,
        settings = settings,
        mealType = fitMealOptions(loggedMeals = loggedMeals, selected = null).first(),
        eatenToday = eatenToday
    ).ideas.size
}

/** The What fits? row inside Today's Remaining card; [count] is null before anything has been logged. */
@Composable
fun WhatFitsRow(
    modifier: Modifier = Modifier,
    count: Int?,
    isLocked: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier
            .heightIn(min = 56.dp)
            .clip(shape = RoundedCornerShape(size = 16.dp))
            .background(color = colors.surface)
            .clickable(role = Role.Button, onClickLabel = stringResource(resource = Res.string.what_fits_open), onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .size(size = 36.dp)
                .background(color = colors.accent, shape = RoundedCornerShape(size = 11.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(painter = painterResource(resource = Res.drawable.ic_bulb), contentDescription = null, tint = colors.onAccent, modifier = Modifier.size(size = 18.dp))
        }
        Column(modifier = Modifier.weight(weight = 1f)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(space = 6.dp)) {
                Text(text = stringResource(resource = Res.string.what_fits), style = typography.bold14, color = colors.ink)
                if (isLocked) ProBadge()
            }
            Text(
                text = when (count) {
                    null -> stringResource(resource = Res.string.what_fits_row_new)
                    0 -> stringResource(resource = Res.string.what_fits_row_none)
                    else -> pluralStringResource(resource = Res.plurals.what_fits_row_ideas, quantity = count, count)
                },
                style = typography.regular11,
                color = colors.muted
            )
        }
        Icon(painter = painterResource(resource = Res.drawable.ic_chevron_right), contentDescription = null, tint = colors.muted, modifier = Modifier.size(size = 16.dp))
    }
}
