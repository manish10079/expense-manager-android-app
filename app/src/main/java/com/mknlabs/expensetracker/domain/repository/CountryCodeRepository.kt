package com.mknlabs.expensetracker.domain.repository

import com.mknlabs.expensetracker.models.CountryCode

interface CountryCodeRepository {
    suspend fun getCountryCodes(): List<CountryCode>
}
