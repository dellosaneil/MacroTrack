package org.thelazybattley.macrotrack.data.repository

import org.thelazybattley.macrotrack.data.local.dao.UserDetailsDao
import org.thelazybattley.macrotrack.data.local.entity.toDomain
import org.thelazybattley.macrotrack.domain.model.UserDetails
import org.thelazybattley.macrotrack.domain.model.toEntity
import org.thelazybattley.macrotrack.domain.repository.UserDetailsRepository

class UserDetailsRepositoryImpl(
    private val userDetailsDao: UserDetailsDao
) : UserDetailsRepository {
    override suspend fun getUserDetails(): UserDetails? {
        return userDetailsDao.getUserDetails()?.toDomain()
    }

    override suspend fun insertUserDetails(
        userDetails: UserDetails
    ) {
        userDetailsDao.insertUserDetails(
            userDetails = userDetails.toEntity()
        )
    }
}
