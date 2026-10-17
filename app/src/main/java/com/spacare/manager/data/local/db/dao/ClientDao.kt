package com.spacare.manager.data.local.db.dao

import androidx.room.*
import com.spacare.manager.data.local.db.entity.ClientEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ClientDao {
    @Query("SELECT * FROM clients")
    fun getAllClients(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients")
    suspend fun getAllClientsSync(): List<ClientEntity>

    @Query("SELECT * FROM clients WHERE id = :id LIMIT 1")
    suspend fun getClientById(id: String): ClientEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(client: ClientEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(clients: List<ClientEntity>)

    @Update
    suspend fun update(client: ClientEntity)

    @Query("DELETE FROM clients WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM clients")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM clients")
    suspend fun getCount(): Int
}

