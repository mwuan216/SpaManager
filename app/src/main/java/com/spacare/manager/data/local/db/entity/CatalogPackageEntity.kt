package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.SpaServicePackage

@Entity(tableName = "catalog_packages")
data class CatalogPackageEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val totalSessions: Int,
    val price: Long,
    val durationMinutes: Int,
    val description: String
) {
    fun toDomain(): SpaServicePackage {
        return SpaServicePackage(
            id = id,
            name = name,
            totalSessions = totalSessions,
            price = price,
            durationMinutes = durationMinutes,
            description = description
        )
    }

    companion object {
        fun fromDomain(pkg: SpaServicePackage): CatalogPackageEntity {
            return CatalogPackageEntity(
                id = pkg.id,
                name = pkg.name,
                totalSessions = pkg.totalSessions,
                price = pkg.price,
                durationMinutes = pkg.durationMinutes,
                description = pkg.description
            )
        }
    }
}

