package org.thelazybattley.macrotrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import org.thelazybattley.macrotrack.data.local.entity.UserDetailsEntity

@Dao
interface UserDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserDetails(userDetails: UserDetailsEntity)

    @Query("SELECT * FROM userdetailsentity LIMIT 1")
    suspend fun getUserDetails(): UserDetailsEntity?
}
