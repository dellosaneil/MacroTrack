package org.thelazybattley.macrotrack.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.toEntity
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

class UserDetailsRepositoryImpl(
    private val dao: UserDetailsDao
) : UserDetailsRepository {
    override suspend fun getUserDetails(): Flow<UserDetails?> {
        return dao.getUserDetails().map { userDetails ->
            userDetails?.toDomain()
        }
    }

    override suspend fun insertUserDetails(
        userDetails: UserDetails
    ) {
        dao.insertUserDetails(
            userDetails = userDetails.toEntity()
        )
    }
}
