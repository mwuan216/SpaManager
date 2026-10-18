package com.spacare.manager.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.spacare.manager.data.local.db.converter.DataConverters
import com.spacare.manager.data.local.db.dao.*
import com.spacare.manager.data.local.db.entity.*

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE appointments ADD COLUMN isSessionDeducted INTEGER NOT NULL DEFAULT 0")
    }
}

@Database(
    entities = [
        ClientEntity::class,
        AppointmentEntity::class,
        TransactionEntity::class,
        StoreProfileEntity::class,
        CatalogPackageEntity::class,
        TechnicianEntity::class
    ],
    version = 2,
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
                .addMigrations(MIGRATION_1_2)
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

