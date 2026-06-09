package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.CompanyDao
import com.blivtech.emptrack.data.local.dao.ShiftDao
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.CompanyData
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.data.model.ShiftData          // ✅ Import ShiftData
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CompanyRepository @Inject constructor(
    private val apiService: ApiService,
    private val companyDao: CompanyDao,
    private val shiftDao: ShiftDao
) {

    fun getCompanies(btCode: String): Flow<List<CompanyEntity>> =
        companyDao.getCompaniesByBtCode(btCode)

    suspend fun getCompanyById(id: Long): CompanyEntity? =
        companyDao.getCompanyById(id)

    fun getShiftsByCompany(companyId: String): Flow<List<ShiftEntity>> =
        shiftDao.getShiftsByCompany(companyId)

    suspend fun createCompany(request: CompanyRequest): Resource<CompanyEntity> {
        return try {
            val response = apiService.createCompany(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val entity = saveCompanyDataToRoom(body.data)
                    Resource.Success(entity)
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Failed to create company. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

    suspend fun updateCompany(id: Long, request: CompanyRequest): Resource<CompanyEntity> {
        return try {
            val response = apiService.updateCompany(id, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    shiftDao.deleteByCompanyId(body.data.companyCode)
                    val entity = saveCompanyDataToRoom(body.data)
                    Resource.Success(entity)
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Failed to update company. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

    suspend fun deleteCompany(id: Long, btCode: String): Resource<Any> {
        return try {
            val response = apiService.deleteCompany(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200) {
                    companyDao.deleteById(id)
                    Resource.Success(Any())
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Failed to delete company. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

    // ─────────────────────────────────────────
    // Helper
    // ─────────────────────────────────────────

    private suspend fun saveCompanyDataToRoom(data: CompanyData): CompanyEntity {

        // ✅ Map CompanyData → CompanyEntity
        val entity = CompanyEntity(
            id          = data.id,
            btCode      = data.btCode,
            companyCode = data.companyCode,
            name        = data.name,
            address     = data.address,
            city        = data.city,
            state       = data.state,
            phone       = data.phone,
            email       = data.email,
            logo        = data.logo,
            status      = data.status
        )
        companyDao.insertAll(listOf(entity))

        // ✅ Explicit type ShiftData on lambda parameter
        val shifts: List<ShiftEntity> = data.shifts.map { s: ShiftData ->
            ShiftEntity(
                id        = s.id,
                btCode    = s.btCode,
                companyCode = s.companyCode,
                shiftCode = s.shiftCode,
                shiftName = s.shiftName,
                startTime = s.startTime,
                endTime   = s.endTime,
                status    = s.status
            )
        }
        shiftDao.insertAll(shifts)

        return entity
    }
}