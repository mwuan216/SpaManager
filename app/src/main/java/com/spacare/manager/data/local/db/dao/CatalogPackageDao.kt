package com.spacare.manager.data.local.db.dao

import androidx.room.*
import com.spacare.manager.data.local.db.entity.CatalogPackageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CatalogPackageDao {
    @Query("SELECT * FROM catalog_packages")
    fun getAllPackages(): Flow<List<CatalogPackageEntity>>

    @Query("SELECT * FROM catalog_packages")
    suspend fun getAllPackagesSync(): List<CatalogPackageEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(pkg: CatalogPackageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(packages: List<CatalogPackageEntity>)

    @Update
    suspend fun update(pkg: CatalogPackageEntity)

    @Query("DELETE FROM catalog_packages WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM catalog_packages")
    suspend fun deleteAll()

    @Query("SELECT COUNT(*) FROM catalog_packages")
    suspend fun getCount(): Int
}

