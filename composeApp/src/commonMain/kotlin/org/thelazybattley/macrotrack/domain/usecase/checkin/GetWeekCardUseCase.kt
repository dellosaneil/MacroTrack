package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import org.thelazybattley.macrotrack.domain.model.WeekBalance

/** The Food Log's This week tile: the balance so far, and whether the check-in has a suggestion not yet seen. */
data class WeekCard(val balance: WeekBalance?, val isNewSuggestion: Boolean)

class GetWeekCardUseCase(
    private val getWeekBalanceUseCase: GetWeekBalanceUseCase,
    private val getWeeklyCheckInUseCase: GetWeeklyCheckInUseCase
) {
    suspend operator fun invoke(): Flow<WeekCard> = combine(
        flow = getWeekBalanceUseCase(),
        flow2 = getWeeklyCheckInUseCase()
    ) { balance, snapshot ->
        WeekCard(balance = balance, isNewSuggestion = snapshot?.isNewSuggestion == true)
    }
}
