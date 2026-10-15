package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface ProRepository {

    fun getTrialStart(): Flow<LocalDate?>

    /** Saves [date] as the trial start unless one is already saved, so the trial starts only once. */
    suspend fun startTrialIfNeeded(date: LocalDate)

    /** The last purchase state the store confirmed, so Pro works offline. */
    fun isPurchased(): Flow<Boolean>

    suspend fun setPurchased(isPurchased: Boolean)

    fun isTrialEndedNoticeDismissed(): Flow<Boolean>

    suspend fun dismissTrialEndedNotice()
}
