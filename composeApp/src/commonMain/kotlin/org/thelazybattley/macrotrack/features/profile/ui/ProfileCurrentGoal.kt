package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.current_goal
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.domain.model.Goal
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileCurrentGoal(
    modifier: Modifier = Modifier,
    currentGoal: Goal?
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.current_goal),
            color = colors.mediumGray,
            style = typography.bold10
        )

        Row(
            modifier = Modifier
                .background(color = colors.white, shape = RoundedCornerShape(size = 14.dp))
                .border(
                    width = 1.dp,
                    color = colors.lightGray,
                    shape = RoundedCornerShape(size = 14.dp)
                )
                .padding(all = 12.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            ProfileGoalCards(
                modifier = Modifier.weight(weight = 1f),
                goal = Goal.LOSE_WEIGHT,
                isSelected = currentGoal == Goal.LOSE_WEIGHT
            )
            ProfileGoalCards(
                modifier = Modifier.weight(weight = 1f),
                goal = Goal.MAINTAIN_WEIGHT,
                isSelected = currentGoal == Goal.MAINTAIN_WEIGHT
            )
            ProfileGoalCards(
                modifier = Modifier.weight(weight = 1f),
                goal = Goal.GAIN_WEIGHT,
                isSelected = currentGoal == Goal.GAIN_WEIGHT
            )
        }
    }
}

@Composable
private fun ProfileGoalCards(
    modifier: Modifier = Modifier,
    goal: Goal,
    isSelected: Boolean,
) {

    val background = if(isSelected) {
        colors.paleBlue
    } else {
        colors.white
    }
    val borderColor = if(isSelected) {
        colors.deepBlue
    } else {
        colors.lightGray
    }
    val contentColor = if(isSelected) {
        colors.deepBlue
    } else {
        colors.black
    }


    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = background,
            contentColor =contentColor
        ),
        border = BorderStroke(width = 1.dp, color = borderColor)
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(space = 4.dp)
        ) {
            Icon(
                painter = painterResource(resource = goal.icon),
                contentDescription = null,
                modifier = Modifier.size(size = 24.dp),
                tint = colors.deepBlue
            )
            Text(
                text = stringResource(resource = goal.title),
                style = typography.bold10,
            )
            Text(
                text = stringResource(resource = goal.description),
                style = typography.regular10,
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xfffffff)
@Composable
private fun PreviewProfileCurrentGoal() {
    MacroTrackTheme {
        ProfileCurrentGoal(
            modifier = Modifier
                .background(color = colors.iceMist)
                .padding(all = 12.dp).fillMaxWidth(),
            currentGoal = Goal.MAINTAIN_WEIGHT
        )
    }
}
