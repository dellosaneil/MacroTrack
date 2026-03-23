package org.thelazybattley.macrotrack.domain.usecase.userdetails

import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

class GetUserDetailsUseCase(
    private val repository: UserDetailsRepository
){
    suspend operator fun invoke() = repository.getUserDetails()
}
