package org.thelazybattley.macrotrack.data.repository

import org.thelazybattley.macrotrack.data.local.dao.BackupDao
import org.thelazybattley.macrotrack.domain.model.DeleteDataStep
import org.thelazybattley.macrotrack.domain.repository.DeleteDataRepository

class DeleteDataRepositoryImpl(private val backupDao: BackupDao) : DeleteDataRepository {

    override suspend fun delete(step: DeleteDataStep) {
        when (step) {
            DeleteDataStep.FOOD_LOG -> backupDao.deleteFoodLogs()
            DeleteDataStep.BODY_RECORDS -> backupDao.deleteBodyRecords()
            DeleteDataStep.SAVED_FOODS -> backupDao.deleteSavedFoods()
            DeleteDataStep.GOALS_AND_SETTINGS -> backupDao.deleteGoalsAndSettings(keepFlagKeys = keptFlagKeys)
        }
    }

    companion object {
        /**
         * Flags that outlive a delete. Pro and the trial belong to the store account, so starting over must not
         * lose a purchase or start a new trial; the stores count rating prompts per install; and someone who
         * turned off usage data expects it to stay off.
         */
        val keptFlagKeys = listOf(
            ProRepositoryImpl.PRO_TRIAL_START,
            ProRepositoryImpl.PRO_PURCHASED,
            ProRepositoryImpl.TRIAL_ENDED_NOTICE_DISMISSED,
            AppFlagRepositoryImpl.REVIEW_LAST_ASKED,
            AppFlagRepositoryImpl.REVIEW_TIMES_ASKED,
            AppFlagRepositoryImpl.TELEMETRY_OPTED_OUT
        )
    }
}
