package org.thelazybattley.macrotrack.features.profile.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import macrotrack.composeapp.generated.resources.Res
import macrotrack.composeapp.generated.resources.bmi
import macrotrack.composeapp.generated.resources.normal
import macrotrack.composeapp.generated.resources.obese
import macrotrack.composeapp.generated.resources.overweight
import macrotrack.composeapp.generated.resources.underweight
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.thelazybattley.macrotrack.features.profile.ProfileBMI
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun ProfileBMI(
    modifier: Modifier = Modifier,
    profileBMI: ProfileBMI,
) {
    val bmiDetails = BMI.toColor(bmi = profileBMI.category)
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = colors.white
        ),
        border = BorderStroke(width = 1.dp, color = colors.lightGray)
    ) {
        Column(
            modifier = Modifier.padding(all = 16.dp),
            verticalArrangement = Arrangement.spacedBy(space = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(resource = Res.string.bmi),
                    color = colors.mediumGray,
                    style = typography.bold10
                )
                Text(
                    text = stringResource(resource = bmiDetails.text),
                    color = bmiDetails.textColor,
                    style = typography.bold10,
                    modifier = Modifier
                        .background(
                            color = bmiDetails.background,
                            shape = RoundedCornerShape(percent = 50)
                        )
                        .padding(all = 6.dp)
                )
            }

            Text(
                text = profileBMI.value.toString(),
                color = colors.black,
                style = typography.bold36
            )
            BMILegend(
                modifier = Modifier.fillMaxWidth(),
                profileBMI = profileBMI
            )
        }
    }
}

@Composable
private fun BMILegend(
    modifier: Modifier = Modifier,
    profileBMI: ProfileBMI
) {
    BoxWithConstraints(modifier = modifier) {
        val obeseOffset = maxWidth * 0.75f
        val overweightOffset = maxWidth * 0.50f
        val normalWeightOffset = maxWidth * 0.25f

        val indicatorOffset = when (profileBMI.category) {
            BMI.UNDERWEIGHT -> {
                profileBMI.progress * maxWidth * 0.25f
            }

            BMI.NORMAL -> {
                profileBMI.progress * maxWidth * 0.5f
            }

            BMI.OVERWEIGHT -> {
                profileBMI.progress * maxWidth * 0.75f
            }

            BMI.OBESE -> {
                profileBMI.progress * maxWidth
            }
        }
        Spacer(
            modifier = Modifier
                .offset(x = obeseOffset)
                .clip(shape = RoundedCornerShape(bottomEnd = 12.dp, topEnd = 12.dp))
                .fillMaxWidth(fraction = 0.25f)
                .height(height = 8.dp)
                .background(color = colors.crimsonRed)
        )
        Spacer(
            modifier = Modifier
                .offset(x = overweightOffset)
                .fillMaxWidth(fraction = 0.25f)
                .height(height = 8.dp)
                .background(color = colors.amber)
        )
        Spacer(
            modifier = Modifier
                .offset(x = normalWeightOffset)
                .fillMaxWidth(fraction = 0.25f)
                .height(height = 8.dp)
                .background(color = colors.cyanBlue)
        )
        Spacer(
            modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 12.dp,
                        bottomStart = 12.dp
                    )
                )
                .fillMaxWidth(fraction = 0.25f)
                .height(height = 8.dp)
                .background(color = colors.emeraldGreen)
        )

        Spacer(
            modifier = Modifier
                .offset(x = indicatorOffset)
                .background(color = colors.black)
                .height(height = 12.dp)
                .width(width = 2.dp)
        )

        Text(
            text = "0",
            color = colors.mediumGray,
            style = typography.regular8,
            modifier = Modifier.offset(y = 8.dp)
        )
        Text(
            text = "18.5",
            color = colors.mediumGray,
            style = typography.regular8,
            modifier = Modifier.offset(
                y = 8.dp,
                x = normalWeightOffset - 8.dp
            )
        )
        Text(
            text = "25",
            color = colors.mediumGray,
            style = typography.regular8,
            modifier = Modifier.offset(
                y = 8.dp,
                x = overweightOffset - 4.dp
            )
        )
        Text(
            text = "30",
            color = colors.mediumGray,
            style = typography.regular8,
            modifier = Modifier.offset(
                y = 8.dp,
                x = obeseOffset - 4.dp
            )
        )
    }
}


enum class BMI(
    val bmiIndex: Float
) {
    UNDERWEIGHT(bmiIndex = 18.5f),
    NORMAL(bmiIndex = 24.9f),
    OVERWEIGHT(bmiIndex = 29.9f),
    OBESE(bmiIndex = 30f);

    companion object {
        @Composable
        fun toColor(bmi: BMI): BmiDetails {
            return when (bmi) {
                UNDERWEIGHT -> {
                    BmiDetails(
                        background = colors.mintGreen,
                        textColor = colors.seafoamGreen,
                        band = colors.emeraldGreen,
                        text = Res.string.underweight
                    )
                }

                NORMAL -> {
                    BmiDetails(
                        background = colors.lightBlue,
                        textColor = colors.azureBlue,
                        band = colors.cyanBlue,
                        text = Res.string.normal
                    )
                }

                OVERWEIGHT -> {
                    BmiDetails(
                        background = colors.cream,
                        textColor = colors.coffeeBrown,
                        band = colors.amber,
                        text = Res.string.overweight
                    )
                }

                OBESE -> {
                    BmiDetails(
                        background = colors.rose,
                        textColor = colors.wineRed,
                        band = colors.crimsonRed,
                        text = Res.string.obese
                    )
                }
            }
        }
    }
}

data class BmiDetails(
    val background: Color,
    val textColor: Color,
    val band: Color,
    val text: StringResource
)

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun PreviewProfileBMI() {
    MacroTrackTheme {
        ProfileBMI(
            modifier = Modifier
                .padding(all = 12.dp).fillMaxWidth(),
            profileBMI = ProfileBMI(
                value = 24.89,
                category = BMI.NORMAL,
                progress = 0.99
            )
        )
    }
}
