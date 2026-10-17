package com.spacare.manager.data.local.db.dao

import androidx.room.*
import com.spacare.manager.data.local.db.entity.StoreProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StoreProfileDao {
    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    fun getStoreProfile(): Flow<StoreProfileEntity?>

    @Query("SELECT * FROM store_profile WHERE id = 1 LIMIT 1")
    suspend fun getStoreProfileSync(): StoreProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(profile: StoreProfileEntity)
}

