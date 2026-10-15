package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import org.thelazybattley.macrotrack.domain.repository.ProRepository

class ProRepositoryImpl(private val dao: AppFlagDao) : ProRepository {

    override fun getTrialStart(): Flow<LocalDate?> = dao.getFlagValue(key = PRO_TRIAL_START).map { value ->
        value?.let { runCatching { LocalDate.parse(input = it) }.getOrNull() }
    }

    override suspend fun startTrialIfNeeded(date: LocalDate) {
        dao.insertFlagIfAbsent(flag = AppFlagEntity(key = PRO_TRIAL_START, value = date.toString()))
    }

    override fun isPurchased(): Flow<Boolean> = dao.isFlagSet(key = PRO_PURCHASED)

    override suspend fun setPurchased(isPurchased: Boolean) {
        if (isPurchased) {
            dao.insertFlag(flag = AppFlagEntity(key = PRO_PURCHASED))
        } else {
            dao.deleteFlag(key = PRO_PURCHASED)
        }
    }

    override fun isTrialEndedNoticeDismissed(): Flow<Boolean> = dao.isFlagSet(key = TRIAL_ENDED_NOTICE_DISMISSED)

    override suspend fun dismissTrialEndedNotice() {
        dao.insertFlag(flag = AppFlagEntity(key = TRIAL_ENDED_NOTICE_DISMISSED))
    }

    companion object {
        const val PRO_TRIAL_START = "pro_trial_start"
        const val PRO_PURCHASED = "pro_purchased"
        const val TRIAL_ENDED_NOTICE_DISMISSED = "pro_trial_ended_notice_dismissed"
    }
}
