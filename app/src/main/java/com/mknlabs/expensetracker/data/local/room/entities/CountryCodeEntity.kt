package com.mknlabs.expensetracker.data.local.room.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mknlabs.expensetracker.models.CountryCode

@Entity(tableName = "country_codes")
data class CountryCodeEntity(
    @PrimaryKey
    val id: Int,
    val country: String,
    @ColumnInfo(name = "dial_code")
    val dialCode: String
) {
    fun toDomain(): CountryCode {
        return CountryCode(
            id = id,
            country = country,
            dialCode = dialCode
        )
    }

    companion object {
        fun fromDomain(domain: CountryCode): CountryCodeEntity {
            return CountryCodeEntity(
                id = domain.id,
                country = domain.country,
                dialCode = domain.dialCode
            )
        }
    }
}
