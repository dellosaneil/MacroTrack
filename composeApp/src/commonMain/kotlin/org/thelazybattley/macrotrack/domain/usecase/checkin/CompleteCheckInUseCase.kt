package org.thelazybattley.macrotrack.domain.usecase.checkin

import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.core.telemetry.Telemetry
import org.thelazybattley.macrotrack.core.telemetry.TelemetryEvent
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

/**
 * Finishes this week's check-in. [measuredMaintenance] is the burn to plan from from now on: the measured one when
 * the suggestion is applied, or what the user already had when they keep their target.
 */
class CompleteCheckInUseCase(
    private val appFlagRepository: AppFlagRepository,
    private val userDetailsRepository: UserDetailsRepository,
    private val telemetry: Telemetry
) {
    suspend operator fun invoke(date: LocalDate, userDetails: UserDetails, measuredMaintenance: Int?) {
        val isApplied = measuredMaintenance != userDetails.measuredMaintenance
        if (isApplied) {
            userDetailsRepository.insertUserDetails(userDetails = userDetails.copy(measuredMaintenance = measuredMaintenance))
        }
        appFlagRepository.setLastCheckIn(date = date)
        telemetry.logEvent(
            name = TelemetryEvent.CHECK_IN_DONE,
            params = mapOf(TelemetryEvent.PARAM_RESULT to if (isApplied) "applied" else "kept")
        )
    }
}
