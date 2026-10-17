package com.spacare.manager.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.spacare.manager.data.local.db.converter.DataConverters
import com.spacare.manager.data.local.db.dao.*
import com.spacare.manager.data.local.db.entity.*

@Database(
    entities = [
        ClientEntity::class,
        AppointmentEntity::class,
        TransactionEntity::class,
        StoreProfileEntity::class,
        CatalogPackageEntity::class,
        TechnicianEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(DataConverters::class)
abstract class SpaDatabase : RoomDatabase() {

    abstract fun clientDao(): ClientDao
    abstract fun appointmentDao(): AppointmentDao
    abstract fun transactionDao(): TransactionDao
    abstract fun storeProfileDao(): StoreProfileDao
    abstract fun catalogPackageDao(): CatalogPackageDao
    abstract fun technicianDao(): TechnicianDao

    companion object {
        @Volatile
        private var INSTANCE: SpaDatabase? = null

        fun getInstance(context: Context): SpaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SpaDatabase::class.java,
                    "spacare_manager.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

