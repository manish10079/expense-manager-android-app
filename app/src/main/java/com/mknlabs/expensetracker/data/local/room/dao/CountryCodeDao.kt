package com.mknlabs.expensetracker.data.local.room.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.mknlabs.expensetracker.data.local.room.entities.CountryCodeEntity

@Dao
interface CountryCodeDao {

    @Query("SELECT * FROM country_codes ORDER BY country ASC")
    suspend fun getCountryCodes(): List<CountryCodeEntity>

    @Upsert
    suspend fun upsertAll(countryCodes: List<CountryCodeEntity>)
}
