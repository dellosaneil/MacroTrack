package org.thelazybattley.macrotrack.domain.usecase.userdetails

import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

class InsertUserDetailsUseCase(private val repository: UserDetailsRepository) {

    suspend operator fun invoke(userDetails: UserDetails) {
        repository.insertUserDetails(userDetails = userDetails)
    }

}
