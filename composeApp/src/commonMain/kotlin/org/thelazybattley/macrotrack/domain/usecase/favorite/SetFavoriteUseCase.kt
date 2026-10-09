package org.thelazybattley.macrotrack.domain.usecase.favorite

import org.thelazybattley.macrotrack.domain.repository.FavoriteRepository

class SetFavoriteUseCase(private val repository: FavoriteRepository) {

    suspend operator fun invoke(key: String, isFavorite: Boolean) =
        repository.setFavorite(key = key, isFavorite = isFavorite)
}
