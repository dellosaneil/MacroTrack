package org.thelazybattley.macrotrack.domain.repository

import org.thelazybattley.macrotrack.domain.model.LibraryFood

interface FoodLibraryRepository {

    /** Every library food, A to Z. Read once from the app's bundled data, then kept in memory. */
    suspend fun getFoods(): List<LibraryFood>
}
