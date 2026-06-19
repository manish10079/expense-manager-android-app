package com.mknlabs.expensetracker.data.repository

import com.mknlabs.expensetracker.data.local.room.dao.CountryCodeDao
import com.mknlabs.expensetracker.domain.repository.CountryCodeRepository
import com.mknlabs.expensetracker.models.CountryCode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CountryCodeRepositoryImpl @Inject constructor(
    private val countryCodeDao: CountryCodeDao
) : CountryCodeRepository {

    override suspend fun getCountryCodes(): List<CountryCode> = withContext(Dispatchers.IO) {
        countryCodeDao.getCountryCodes().map { it.toDomain() }
    }
}
