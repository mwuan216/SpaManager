package com.spacare.manager.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.spacare.manager.domain.model.BankOption
import com.spacare.manager.domain.model.StoreProfile

@Entity(tableName = "store_profile")
data class StoreProfileEntity(
    @PrimaryKey
    val id: Int = 1,
    val name: String,
    val hotline: String,
    val address: String,
    val bankBin: String,
    val bankShortName: String,
    val bankFullName: String,
    val accountNumber: String,
    val accountHolder: String,
    val defaultMemo: String
) {
    fun toDomain(): StoreProfile {
        return StoreProfile(
            name = name,
            hotline = hotline,
            address = address,
            selectedBank = BankOption(bin = bankBin, shortName = bankShortName, fullName = bankFullName),
            accountNumber = accountNumber,
            accountHolder = accountHolder,
            defaultMemo = defaultMemo
        )
    }

    companion object {
        fun fromDomain(profile: StoreProfile): StoreProfileEntity {
            return StoreProfileEntity(
                id = 1,
                name = profile.name,
                hotline = profile.hotline,
                address = profile.address,
                bankBin = profile.selectedBank.bin,
                bankShortName = profile.selectedBank.shortName,
                bankFullName = profile.selectedBank.fullName,
                accountNumber = profile.accountNumber,
                accountHolder = profile.accountHolder,
                defaultMemo = profile.defaultMemo
            )
        }
    }
}

