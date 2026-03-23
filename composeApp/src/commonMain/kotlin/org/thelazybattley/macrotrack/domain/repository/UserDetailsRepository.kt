package org.thelazybattley.macrotrack.domain.repository

import org.thelazybattley.macrotrack.domain.model.UserDetails

interface UserDetailsRepository {

    suspend fun getUserDetails(): UserDetails?

    suspend fun insertUserDetails(
        userDetails: UserDetails
    )

}
