package com.spacare.manager.data.local.db.dao

import androidx.room.*
import com.spacare.manager.data.local.db.entity.TechnicianEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TechnicianDao {
    @Query("SELECT * FROM technicians")
    fun getAllTechnicians(): Flow<List<TechnicianEntity>>

    @Query("SELECT * FROM technicians")
    suspend fun getAllTechniciansSync(): List<TechnicianEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tech: TechnicianEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(techs: List<TechnicianEntity>)

    @Update
    suspend fun update(tech: TechnicianEntity)

    @Query("DELETE FROM technicians WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM technicians")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM technicians")
    suspend fun getCount(): Int
}

