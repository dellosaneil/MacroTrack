package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.MacroGoals
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

/**
 * Finishes this week's check-in. [measuredMaintenance] is the burn to plan from from now on: the measured one when
 * the suggestion is applied, or what the user already had when they keep their target. With custom targets,
 * applying also moves [customMacroGoals] to the suggested calories.
 */
class CompleteCheckInUseCase(
    private val appFlagRepository: AppFlagRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(
        date: LocalDate,
        userDetails: UserDetails,
        measuredMaintenance: Int?,
        customMacroGoals: MacroGoals? = userDetails.customMacroGoals
    ) {
        val isApplied = measuredMaintenance != userDetails.measuredMaintenance || customMacroGoals != userDetails.customMacroGoals
        if (isApplied) {
            userDetailsRepository.insertUserDetails(
                userDetails = userDetails.copy(measuredMaintenance = measuredMaintenance, customMacroGoals = customMacroGoals)
            )
        }
        appFlagRepository.setLastCheckIn(date = date)
        telemetry.logEvent(
            name = TelemetryEvent.CHECK_IN_DONE,
            params = mapOf(TelemetryEvent.PARAM_RESULT to if (isApplied) "applied" else "kept")
        )
    }
}
