package org.thelazybattley.macrotrack.features.nextday

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.getCurrentDate
import org.thelazybattley.macrotrack.domain.model.DinnerEffect
import org.thelazybattley.macrotrack.domain.model.DinnerSort
import org.thelazybattley.macrotrack.domain.model.FoodDinnersAverage
import org.thelazybattley.macrotrack.domain.model.NextDayDinner
import org.thelazybattley.macrotrack.domain.model.NextDayOverview
import org.thelazybattley.macrotrack.domain.model.NextDayPeriod
import org.thelazybattley.macrotrack.domain.model.foodAverage
import org.thelazybattley.macrotrack.domain.model.nextDayOverview
import org.thelazybattley.macrotrack.domain.model.sortedFor
import org.thelazybattley.macrotrack.domain.usecase.nextday.GetNextDayInputsUseCase
import org.thelazybattley.macrotrack.domain.usecase.nextday.NextDayInputs
import org.thelazybattley.macrotrack.ui.navigation.AppDestinations

/** All dinners show this many at a time. */
const val NEXT_DAY_PAGE = 10

/** Every dinner over [period]: searched by a food in it, filtered to one [filter] (all when null), sorted, paged. */
data class NextDayAllViewState(
    val today: LocalDate = getCurrentDate(),
    val inputs: NextDayInputs? = null,
    val period: NextDayPeriod = NextDayPeriod.YEAR,
    val query: String = "",
    val filter: DinnerEffect? = null,
    val sort: DinnerSort = DinnerSort.MOST_UP,
    val limit: Int = NEXT_DAY_PAGE
) {
    val overview: NextDayOverview? =
        inputs?.let { nextDayOverview(items = it.items, weights = it.weights, today = today, period = period) }

    /** Dinners with the searched food, before the filter. */
    val matching: List<NextDayDinner> = overview?.matching(query = query).orEmpty()

    val filtered: List<NextDayDinner> = matching.filter { filter == null || it.effect == filter }.sortedFor(sort = sort)

    val shown: List<NextDayDinner> get() = filtered.take(n = limit)

    val hasMore: Boolean get() = filtered.size > limit

    /** What dinners with the searched food do on average; null without a search or a dinner with a number. */
    val foodAverage: FoodDinnersAverage? get() = if (query.isBlank()) null else matching.foodAverage()

    fun count(effect: DinnerEffect?): Int = if (effect == null) matching.size else matching.count { it.effect == effect }

    fun unitOf(name: String, isRecipe: Boolean): String = inputs?.unitOf(name = name, isRecipe = isRecipe) ?: "g"
}

interface NextDayAllCallbacks {
    fun onQueryChanged(query: String)
    fun onFilterSelected(filter: DinnerEffect?)
    fun onSortSelected(sort: DinnerSort)
    fun onShowMore()

    companion object {
        fun default() = object : NextDayAllCallbacks {
            override fun onQueryChanged(query: String) = Unit
            override fun onFilterSelected(filter: DinnerEffect?) = Unit
            override fun onSortSelected(sort: DinnerSort) = Unit
            override fun onShowMore() = Unit
        }
    }
}

class NextDayAllViewModel(
    getNextDayInputsUseCase: GetNextDayInputsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel(), NextDayAllCallbacks {

    private val _state = MutableStateFlow(
        value = savedStateHandle.get<String>(key = AppDestinations.DINNER_FILTER)
            .let { name -> DinnerEffect.entries.find { it.name == name } }
            .let { filter ->
                NextDayAllViewState(
                    period = nextDayPeriodOf(argument = savedStateHandle.get<String>(key = AppDestinations.NEXT_DAY_PERIOD)),
                    query = savedStateHandle.get<String>(key = AppDestinations.QUERY).orEmpty(),
                    filter = filter,
                    // A bucket opens on the order that suits it.
                    sort = when (filter) {
                        DinnerEffect.LOWERS -> DinnerSort.MOST_DOWN
                        DinnerEffect.EVEN, DinnerEffect.WAITING -> DinnerSort.MOST_OFTEN
                        else -> DinnerSort.MOST_UP
                    }
                )
            }
    )
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            getNextDayInputsUseCase().collect { inputs -> _state.update { it.copy(inputs = inputs) } }
        }
    }

    override fun onQueryChanged(query: String) {
        _state.update { it.copy(query = query, limit = NEXT_DAY_PAGE) }
    }

    override fun onFilterSelected(filter: DinnerEffect?) {
        _state.update { it.copy(filter = filter, limit = NEXT_DAY_PAGE) }
    }

    override fun onSortSelected(sort: DinnerSort) {
        _state.update { it.copy(sort = sort) }
    }

    override fun onShowMore() {
        _state.update { it.copy(limit = it.limit + NEXT_DAY_PAGE) }
    }
}

/** The period a [AppDestinations.NEXT_DAY_PERIOD] argument names, or a year when there is none. */
fun nextDayPeriodOf(argument: String?): NextDayPeriod = NextDayPeriod.entries.find { it.name == argument } ?: NextDayPeriod.YEAR
