package org.thelazybattley.macrotrack.domain.usecase.dailygoal

import org.thelazybattley.macrotrack.domain.model.DailyGoal
import org.thelazybattley.macrotrack.domain.repository.DailyGoalRepository

class SaveDailyGoalUseCase(private val repository: DailyGoalRepository) {

    suspend operator fun invoke(dailyGoal: DailyGoal) {
        repository.saveDailyGoal(dailyGoal = dailyGoal)
    }
}
