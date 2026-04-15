package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.account
import macrotrack.composeapp.generated.resources.ic_chevron_left
import macrotrack.composeapp.generated.resources.ic_log
import macrotrack.composeapp.generated.resources.ic_profile
import macrotrack.composeapp.generated.resources.ic_star
import macrotrack.composeapp.generated.resources.nutrition_goals
import macrotrack.composeapp.generated.resources.personal_information
import macrotrack.composeapp.generated.resources.weight_history
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.ui.common.CommonSurface
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileAccount(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Text(
            text = stringResource(resource = Res.string.account),
            color = colors.mediumGray,
            style = typography.bold10
        )
        CommonSurface {
            Column(
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = colors.lightGray,
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .padding(all = 12.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(space = 8.dp)
            ) {
                ProfileSectionEnum.entries.forEachIndexed { index, section ->
                    ProfileSection(
                        modifier = Modifier.fillMaxWidth(),
                        section = section
                    )
                    if (index != ProfileSectionEnum.entries.lastIndex) {
                        HorizontalDivider(
                            thickness = 1.dp,
                            color = colors.lightGray
                        )
                    }
                }
            }
        }
    }
}


@Composable
private fun ProfileSection(
    modifier: Modifier = Modifier,
    section: ProfileSectionEnum
) {
    val sectionUiDetails = ProfileSectionEnum.details(section = section)
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp)
    ) {
        Icon(
            painter = painterResource(resource = sectionUiDetails.icon),
            contentDescription = null,
            modifier = Modifier
                .background(
                    color = sectionUiDetails.backgroundColor,
                    shape = RoundedCornerShape(size = 8.dp)
                )
                .padding(all = 8.dp).size(size = 16.dp),
            tint = sectionUiDetails.iconColor
        )
        Text(
            text = stringResource(resource = sectionUiDetails.text),
            style = typography.bold14,
            color = colors.black
        )
        Spacer(modifier = Modifier.weight(weight = 1f))
        Icon(
            painter = painterResource(resource = Res.drawable.ic_chevron_left),
            contentDescription = null,
            modifier = Modifier
                .size(size = 14.dp)
                .rotate(degrees = 180f),
            tint = colors.lightGray,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewProfileAccount() {
    MacroTrackTheme {
        ProfileAccount(
            modifier = Modifier.fillMaxWidth().padding(all = 12.dp)
        )
    }
}


enum class ProfileSectionEnum {
    PERSONAL_INFO,
    NUTRITION_GOALS,
    WEIGHT_HISTORY;

    companion object {
        @Composable
        fun details(section: ProfileSectionEnum): ProfileSectionUi {
            return when (section) {
                PERSONAL_INFO -> {
                    ProfileSectionUi(
                        text = Res.string.personal_information,
                        icon = Res.drawable.ic_profile,
                        iconColor = colors.deepBlue,
                        backgroundColor = colors.deepBlue.copy(
                            alpha = 0.2f
                        )
                    )
                }

                NUTRITION_GOALS -> {
                    ProfileSectionUi(
                        text = Res.string.nutrition_goals,
                        icon = Res.drawable.ic_star,
                        iconColor = colors.green,
                        backgroundColor = colors.green.copy(
                            alpha = 0.2f
                        )
                    )
                }

                WEIGHT_HISTORY -> {
                    ProfileSectionUi(
                        text = Res.string.weight_history,
                        icon = Res.drawable.ic_log,
                        iconColor = colors.orange,
                        backgroundColor = colors.orange.copy(
                            alpha = 0.2f
                        )
                    )
                }
            }
        }
    }
}


data class ProfileSectionUi(
    val text: StringResource,
    val icon: DrawableResource,
    val iconColor: Color,
    val backgroundColor: Color
)
