package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity

@Dao
interface UserDetailsDao {

    @Upsert
    suspend fun insertUserDetails(userDetails: UserDetailsEntity)

    @Query("SELECT * FROM userdetailsentity LIMIT 1")
    suspend fun getUserDetails(): UserDetailsEntity?
}
