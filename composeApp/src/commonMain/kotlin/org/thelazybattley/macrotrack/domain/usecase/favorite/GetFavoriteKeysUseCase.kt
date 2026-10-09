package org.thelazybattley.macrotrack.domain.usecase.favorite

import org.thelazybattley.macrotrack.domain.repository.FavoriteRepository

class GetFavoriteKeysUseCase(private val repository: FavoriteRepository) {

    operator fun invoke() = repository.getFavoriteKeys()
}
