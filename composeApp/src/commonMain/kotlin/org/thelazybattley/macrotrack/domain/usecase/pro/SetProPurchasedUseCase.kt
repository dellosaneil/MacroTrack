package org.thelazybattley.macrotrack.domain.usecase.pro

import org.thelazybattley.macrotrack.domain.repository.ProRepository

class SetProPurchasedUseCase(private val repository: ProRepository) {

    suspend operator fun invoke(isPurchased: Boolean) = repository.setPurchased(isPurchased = isPurchased)
}
