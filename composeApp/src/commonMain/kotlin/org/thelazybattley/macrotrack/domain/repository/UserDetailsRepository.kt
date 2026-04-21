package org.thelazybattley.macrotrack.domain.repository

import kotlinx.coroutines.flow.Flow
import org.thelazybattley.macrotrack.domain.model.UserDetails

interface UserDetailsRepository {

    suspend fun getUserDetails(): Flow<UserDetails?>

    suspend fun insertUserDetails(
        userDetails: UserDetails
    )

}
