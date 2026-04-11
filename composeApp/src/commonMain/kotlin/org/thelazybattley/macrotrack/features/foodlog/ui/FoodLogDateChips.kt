package org.thelazybattley.macrotrack.features.foodlog.ui

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.plus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.colors
import org.thelazybattley.macrotrack.ui.theme.MacroTrackTheme.typography

@Composable
fun FoodLogDateChips(
    modifier: Modifier = Modifier,
    dates: List<LocalDate>,
    dateSelected: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
) {
    LazyRow(
        modifier = modifier
            .border(width = 1.dp, color = colors.lightGray, shape = RoundedCornerShape(size = 8.dp))
            .padding(all = 10.dp),
        horizontalArrangement = Arrangement.spacedBy(space = 8.dp),
    ) {
        items(items = dates, key = { it.toEpochDays() }) { date ->
            DateChips(
                modifier = Modifier,
                date = date,
                onClick = { onDateSelected(date) },
                isSelected = dateSelected == date
            )
        }
    }
}

@Composable
private fun DateChips(
    modifier: Modifier = Modifier,
    date: LocalDate,
    onClick: () -> Unit,
    isSelected: Boolean
) {
    val background = if (isSelected) colors.deepBlue else colors.iceMist
    val textColor = if(isSelected) colors.white else colors.mediumGray

    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = background
        ),
        shape = RoundedCornerShape(size = 6.dp),
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(space = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)
        ) {
            Text(
                modifier = Modifier,
                text = date.dayOfWeek.name.take(n = 3),
                color = colors.mediumGray,
                style = typography.bold13
            )
            Text(
                text = date.day.toString(),
                color = textColor,
                style = typography.bold13
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xffffffff)
@Composable
private fun Preview() {
    MacroTrackTheme {
        FoodLogDateChips(
            modifier = Modifier.padding(all = 12.dp),
            dateSelected = getCurrentDate(),
            dates = listOf(
                getCurrentDate(),
                getCurrentDate().plus(period = DatePeriod(days = 1)),
                getCurrentDate().plus(period = DatePeriod(days = 2)),
                getCurrentDate().plus(period = DatePeriod(days = 3)),
                getCurrentDate().plus(period = DatePeriod(days = 4)),
                getCurrentDate().plus(period = DatePeriod(days = 5)),
                getCurrentDate().plus(period = DatePeriod(days = 6)),
                getCurrentDate().plus(period = DatePeriod(days = 7)),
            )
        ) {

        }
    }
}
