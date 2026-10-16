package org.thelazybattley.macrotrack.domain.usecase.library

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.thelazybattley.macrotrack.domain.model.LibraryFood
import org.thelazybattley.macrotrack.domain.repository.FoodLibraryRepository

class GetFoodLibraryUseCase(private val repository: FoodLibraryRepository) {

    operator fun invoke(): Flow<List<LibraryFood>> = flow { emit(value = repository.getFoods()) }
}
