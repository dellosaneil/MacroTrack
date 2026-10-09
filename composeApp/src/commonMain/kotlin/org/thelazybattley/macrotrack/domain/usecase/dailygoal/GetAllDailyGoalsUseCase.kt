package org.thelazybattley.macrotrack.domain.usecase.dailygoal

import org.thelazybattley.macrotrack.domain.repository.DailyGoalRepository

class GetAllDailyGoalsUseCase(private val repository: DailyGoalRepository) {

    operator fun invoke() = repository.getAllDailyGoals()
}
