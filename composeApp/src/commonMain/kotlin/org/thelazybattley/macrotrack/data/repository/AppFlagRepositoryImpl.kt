package org.thelazybattley.macrotrack.data.repository

import androidx.compose.ui.text.intl.Locale
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.datetime.LocalDate
import org.thelazybattley.macrotrack.data.local.dao.AppFlagDao
import org.thelazybattley.macrotrack.data.local.entity.AppFlagEntity
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import org.thelazybattley.macrotrack.domain.model.CustomMilestone
import org.thelazybattley.macrotrack.domain.model.ForecastSettings
import org.thelazybattley.macrotrack.domain.model.ReviewHistory
import org.thelazybattley.macrotrack.domain.model.UnitSystem
import org.thelazybattley.macrotrack.domain.model.WhatFitsSettings
import org.thelazybattley.macrotrack.domain.repository.AppFlagRepository

class AppFlagRepositoryImpl(private val dao: AppFlagDao) : AppFlagRepository {

    override fun isTourCompleted(): Flow<Boolean> = dao.isFlagSet(key = TOUR_COMPLETED)

    override suspend fun setTourCompleted(isCompleted: Boolean) {
        if (isCompleted) {
            dao.insertFlag(flag = AppFlagEntity(key = TOUR_COMPLETED))
        } else {
            dao.deleteFlag(key = TOUR_COMPLETED)
        }
    }

    override fun getUnitSystem(): Flow<UnitSystem> = dao.getFlagValue(key = UNIT_SYSTEM).map { value ->
        UnitSystem.entries.firstOrNull { it.name == value } ?: UnitSystem.defaultFor(region = Locale.current.region)
    }

    override suspend fun setUnitSystem(unitSystem: UnitSystem) {
        dao.insertFlag(flag = AppFlagEntity(key = UNIT_SYSTEM, value = unitSystem.name))
    }

    override fun isTelemetryEnabled(): Flow<Boolean> = dao.isFlagSet(key = TELEMETRY_OPTED_OUT).map { !it }

    override suspend fun setTelemetryEnabled(isEnabled: Boolean) {
        if (isEnabled) {
            dao.deleteFlag(key = TELEMETRY_OPTED_OUT)
        } else {
            dao.insertFlag(flag = AppFlagEntity(key = TELEMETRY_OPTED_OUT))
        }
    }

    override fun getReviewHistory(): Flow<ReviewHistory> = combine(
        flow = dao.getFlagValue(key = REVIEW_LAST_ASKED),
        flow2 = dao.getFlagValue(key = REVIEW_TIMES_ASKED)
    ) { lastAsked, timesAsked ->
        ReviewHistory(
            lastAsked = lastAsked?.let { runCatching { LocalDate.parse(input = it) }.getOrNull() },
            timesAsked = timesAsked?.toIntOrNull() ?: 0
        )
    }

    override suspend fun recordReviewAsked(date: LocalDate) {
        val timesAsked = getReviewHistory().first().timesAsked
        dao.insertFlag(flag = AppFlagEntity(key = REVIEW_LAST_ASKED, value = date.toString()))
        dao.insertFlag(flag = AppFlagEntity(key = REVIEW_TIMES_ASKED, value = (timesAsked + 1).toString()))
    }

    override fun getSeenCheckInSuggestion(): Flow<Int?> =
        dao.getFlagValue(key = SEEN_CHECK_IN_SUGGESTION).map { value -> value?.toIntOrNull() }

    override suspend fun setSeenCheckInSuggestion(calories: Int) {
        dao.insertFlag(flag = AppFlagEntity(key = SEEN_CHECK_IN_SUGGESTION, value = calories.toString()))
    }

    override fun isWidgetWeightShown(): Flow<Boolean> = dao.isFlagSet(key = WIDGET_WEIGHT_HIDDEN).map { !it }

    override suspend fun setWidgetWeightShown(isShown: Boolean) {
        if (isShown) {
            dao.deleteFlag(key = WIDGET_WEIGHT_HIDDEN)
        } else {
            dao.insertFlag(flag = AppFlagEntity(key = WIDGET_WEIGHT_HIDDEN))
        }
    }

    override fun isWidgetLockNumbersShown(): Flow<Boolean> = dao.isFlagSet(key = WIDGET_LOCK_NUMBERS_HIDDEN).map { !it }

    override suspend fun setWidgetLockNumbersShown(isShown: Boolean) {
        if (isShown) {
            dao.deleteFlag(key = WIDGET_LOCK_NUMBERS_HIDDEN)
        } else {
            dao.insertFlag(flag = AppFlagEntity(key = WIDGET_LOCK_NUMBERS_HIDDEN))
        }
    }

    override fun getForecastSettings(): Flow<ForecastSettings> = dao.getFlagValue(key = FORECAST_SETTINGS).map { value ->
        value?.let { runCatching { json.decodeFromString(ForecastSettings.serializer(), it) }.getOrNull() } ?: ForecastSettings()
    }

    override suspend fun setForecastSettings(settings: ForecastSettings) {
        dao.insertFlag(flag = AppFlagEntity(key = FORECAST_SETTINGS, value = json.encodeToString(ForecastSettings.serializer(), settings)))
    }

    override fun getCustomMilestones(): Flow<List<CustomMilestone>> = dao.getFlagValue(key = FORECAST_MILESTONES).map { value ->
        value?.let { runCatching { json.decodeFromString(milestonesSerializer, it) }.getOrNull() }.orEmpty()
    }

    override suspend fun setCustomMilestones(milestones: List<CustomMilestone>) {
        dao.insertFlag(flag = AppFlagEntity(key = FORECAST_MILESTONES, value = json.encodeToString(milestonesSerializer, milestones)))
    }

    override fun getWhatFitsSettings(): Flow<WhatFitsSettings> = dao.getFlagValue(key = WHAT_FITS_SETTINGS).map { value ->
        value?.let { runCatching { json.decodeFromString(WhatFitsSettings.serializer(), it) }.getOrNull() } ?: WhatFitsSettings()
    }

    override suspend fun setWhatFitsSettings(settings: WhatFitsSettings) {
        dao.insertFlag(flag = AppFlagEntity(key = WHAT_FITS_SETTINGS, value = json.encodeToString(WhatFitsSettings.serializer(), settings)))
    }

    companion object {
        private val json = Json { ignoreUnknownKeys = true }
        private val milestonesSerializer = ListSerializer(CustomMilestone.serializer())

        /** Also seeded by AppDatabase.MIGRATION_8_9 for people who set up the app before the tour existed. */
        const val TOUR_COMPLETED = "tour_completed"
        const val UNIT_SYSTEM = "unit_system"
        const val TELEMETRY_OPTED_OUT = "telemetry_opted_out"
        const val REVIEW_LAST_ASKED = "review_last_asked"
        const val REVIEW_TIMES_ASKED = "review_times_asked"
        const val SEEN_CHECK_IN_SUGGESTION = "seen_check_in_suggestion"
        const val WIDGET_WEIGHT_HIDDEN = "widget_weight_hidden"
        const val WIDGET_LOCK_NUMBERS_HIDDEN = "widget_lock_numbers_hidden"
        const val FORECAST_SETTINGS = "forecast_settings"
        const val FORECAST_MILESTONES = "forecast_milestones"
        const val WHAT_FITS_SETTINGS = "what_fits_settings"
    }
}
