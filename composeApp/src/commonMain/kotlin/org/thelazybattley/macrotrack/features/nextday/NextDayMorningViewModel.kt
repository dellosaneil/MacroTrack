package org.thelazybattley.macrotrack.features.nextday

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.datetime.DatePeriod
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.MorningExplanation
import org.thelazybattley.macrotrack.domain.model.Weight
import org.thelazybattley.macrotrack.domain.model.morningExplanation
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.features.profile.weighthistory.ui.MovingAverageWindow
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverage
import org.thelazybattley.macrotrack.features.profile.weighthistory.movingAverageOn
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations
import org.thelazybattley.macrotrack.ui.navigation.logDateOf

/** Days on the trend chart, the weigh-in's day included. */
const val MORNING_CHART_DAYS = 7

/** The trend rising less than this, in kg, since the day before still reads as "didn't move up". */
private const val TREND_FLAT_KG = 0.05

/**
 * The weigh-in saved for [date] and, in [explanation], the dinner that explains its jump. [days] are the
 * [MORNING_CHART_DAYS] days up to it, each with its weigh-in (if any) and the 7-day moving average (the trend).
 */
data class NextDayMorningViewState(
    val date: LocalDate,
    val today: LocalDate = getCurrentDate(),
    val isLoaded: Boolean = false,
    val weight: Double? = null,
    /** Since the weigh-in the day before; null without one. */
    val change: Double? = null,
    val explanation: MorningExplanation? = null,
    val days: List<MorningDay> = emptyList(),
    /** The trend a week before [date]; null when nothing was logged by then. */
    val trendWeekAgo: Double? = null,
    val units: Map<String, String> = emptyMap()
) {
    fun unitOf(name: String, isRecipe: Boolean): String = if (isRecipe) "" else units[name] ?: "g"

    val trend: Double? get() = days.lastOrNull()?.trend

    /** The trend now minus a week ago. */
    val trendWeekChange: Double? get() = trendWeekAgo?.let { weekAgo -> trend?.let { it - weekAgo } }

    val trendYesterday: Double? get() = days.getOrNull(index = days.lastIndex - 1)?.trend

    val isTrendUp: Boolean
        get() {
            val now = trend ?: return false
            val before = trendYesterday ?: return false
            return now - before >= TREND_FLAT_KG
        }
}

data class MorningDay(val date: LocalDate, val weight: Double?, val trend: Double?)

class NextDayMorningViewModel(
    getNextDayInputsUseCase: GetNextDayInputsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val date = logDateOf(argument = savedStateHandle.get<String>(key = AppDestinations.LOG_DATE))

    val state = getNextDayInputsUseCase()
        .map { inputs ->
            morningState(
                date = date,
                weights = inputs.weights,
                explanation = morningExplanation(date = date, items = inputs.items, weights = inputs.weights)
            ).copy(units = inputs.units)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
            initialValue = NextDayMorningViewState(date = date)
        )
}

/** The saved weigh-in on [date] with the week of weigh-ins and trend leading up to it. */
fun morningState(date: LocalDate, weights: List<Weight>, explanation: MorningExplanation?): NextDayMorningViewState {
    val byDate = weights.associate { it.date to it.weight }
    val window = MovingAverageWindow.SEVEN.days
    val trendByDate = weights.filter { it.date <= date }.movingAverage(windowDays = window).associate { it.date to it.weight }
    val days = (MORNING_CHART_DAYS - 1 downTo 0).map { back ->
        val day = date.minus(period = DatePeriod(days = back))
        MorningDay(date = day, weight = byDate[day], trend = trendByDate[day] ?: weights.movingAverageOn(date = day, windowDays = window))
    }
    val weight = byDate[date]
    val before = byDate[date.minus(period = DatePeriod(days = 1))]
    return NextDayMorningViewState(
        date = date,
        isLoaded = true,
        weight = weight,
        change = if (weight != null && before != null) weight - before else null,
        explanation = explanation,
        days = days,
        trendWeekAgo = weights.movingAverageOn(date = date.minus(period = DatePeriod(days = MORNING_CHART_DAYS)), windowDays = window)
    )
}
