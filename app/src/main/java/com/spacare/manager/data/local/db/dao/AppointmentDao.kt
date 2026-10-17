package com.spacare.manager.data.local.db.dao

import androidx.room.*
import com.spacare.manager.data.local.db.entity.AppointmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppointmentDao {
    @Query("SELECT * FROM appointments")
    fun getAllAppointments(): Flow<List<AppointmentEntity>>

    @Query("SELECT * FROM appointments")
    suspend fun getAllAppointmentsSync(): List<AppointmentEntity>

    @Query("SELECT * FROM appointments WHERE date = :date")
    suspend fun getAppointmentsByDate(date: String): List<AppointmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(appointment: AppointmentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(appointments: List<AppointmentEntity>)

    @Update
    suspend fun update(appointment: AppointmentEntity)

    @Query("DELETE FROM appointments WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM appointments")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM appointments")
    suspend fun getCount(): Int
}

