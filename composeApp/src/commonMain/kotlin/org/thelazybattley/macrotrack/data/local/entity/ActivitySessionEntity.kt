package org.thelazybattley.macrotrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable
import org.thelazybattley.macrotrack.domain.model.ActivitySession
import org.thelazybattley.macrotrack.domain.model.ActivityType
import org.thelazybattley.macrotrack.domain.model.RoutePoint

/** Added by AppDatabase.MIGRATION_16_17. The route stays on the phone and goes into backups. */
@Entity
@Serializable
data class ActivitySessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: ActivityType,
    val name: String? = null,
    val startMillis: Long,
    val endMillis: Long,
    val movingSeconds: Int,
    val distanceMeters: Double,
    val steps: Int,
    val climbMeters: Double,
    val route: List<RoutePoint>
)

fun ActivitySessionEntity.toDomain() = ActivitySession(
    id = id,
    type = type,
    name = name,
    startMillis = startMillis,
    endMillis = endMillis,
    movingSeconds = movingSeconds,
    distanceMeters = distanceMeters,
    steps = steps,
    climbMeters = climbMeters,
    route = route
)

fun ActivitySession.toEntity() = ActivitySessionEntity(
    id = id,
    type = type,
    name = name,
    startMillis = startMillis,
    endMillis = endMillis,
    movingSeconds = movingSeconds,
    distanceMeters = distanceMeters,
    steps = steps,
    climbMeters = climbMeters,
    route = route
)
