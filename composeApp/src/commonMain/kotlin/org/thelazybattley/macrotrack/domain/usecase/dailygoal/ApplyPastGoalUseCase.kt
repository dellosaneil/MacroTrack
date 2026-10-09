package org.thelazybattley.macrotrack.domain.usecase.dailygoal

import org.thelazybattley.macrotrack.domain.model.PastGoalChange
import org.thelazybattley.macrotrack.domain.repository.DailyGoalRepository

/** Writes a [PastGoalChange]: the edited range is cleared, then its start and the day after are recorded. */
class ApplyPastGoalUseCase(private val repository: DailyGoalRepository) {

    suspend operator fun invoke(change: PastGoalChange) {
        change.following?.let { repository.saveDailyGoal(dailyGoal = it) }
        repository.deleteDailyGoals(start = change.start, end = change.end)
        repository.saveDailyGoal(dailyGoal = change.newGoal)
    }
}
