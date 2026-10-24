package org.thelazybattley.macrotrack.core.platform

import org.thelazybattley.macrotrack.domain.model.PhotoAlbum

/** Sets the photo reminders: one at each album's time on its chosen days, replacing any set before. */
interface PhotoReminderScheduler {
    suspend fun schedule(albums: List<PhotoAlbum>)
}
