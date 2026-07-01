package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class WageReportRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ═══════════════════════════════════════════
    // ADVANCE
    // ═══════════════════════════════════════════

    fun getAdvanceList(
        btCode: String, companyCode: String, month: String
    ): Flow<Resource<AdvanceMonthlyDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAdvanceList(btCode, companyCode, month)
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> emit(Resource.Success(body.data))
                response.isSuccessful -> emit(Resource.Error(body?.message ?: "No data"))
                else -> emit(Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to load advance report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun updateAdvance(
        advanceId: String,
        btCode: String,
        companyCode: String,
        empCode: String,
        requestDate: String,
        amount: Double,
        repayMonth: String,
        remarks: String
    ): Resource<AdvanceEntryDto> {
        return try {
            val response = apiService.updateAdvance(
                advanceId,
                AdvanceEntryRequest(
                    btCode      = btCode,
                    companyCode = companyCode,
                    empCode     = empCode,
                    requestDate = requestDate,
                    amount      = amount,
                    repayMonth  = repayMonth,
                    remarks     = remarks
                )
            )
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> Resource.Success(body.data)
                response.isSuccessful -> Resource.Error(body?.message ?: "No data")
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to update advance"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteAdvance(
        advanceId: String,
        btCode: String,
        companyCode: String
    ): Resource<Unit> {
        return try {
            val response = apiService.deleteAdvance(advanceId, btCode, companyCode)
            val body = response.body()
            when {
                response.isSuccessful -> Resource.Success(Unit)
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to delete advance"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ═══════════════════════════════════════════
    // BONUS
    // ═══════════════════════════════════════════

    fun getBonusList(
        btCode: String, companyCode: String, month: String
    ): Flow<Resource<BonusMonthlyDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getBonusList(btCode, companyCode, month)
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> emit(Resource.Success(body.data))
                response.isSuccessful -> emit(Resource.Error(body?.message ?: "No data"))
                else -> emit(Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to load bonus report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun updateBonus(
        bonusId: String,
        btCode: String,
        companyCode: String,
        empCode: String,
        bonusDate: String,
        bonusType: String,
        amount: Double,
        remarks: String
    ): Resource<BonusEntryDto> {
        return try {
            val response = apiService.updateBonus(
                bonusId,
                BonusEntryRequest(
                    btCode      = btCode,
                    companyCode = companyCode,
                    empCode     = empCode,
                    bonusDate   = bonusDate,
                    bonusType   = bonusType,
                    amount      = amount,
                    remarks     = remarks
                )
            )
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> Resource.Success(body.data)
                response.isSuccessful -> Resource.Error(body?.message ?: "No data")
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to update bonus"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteBonus(
        bonusId: String,
        btCode: String,
        companyCode: String
    ): Resource<Unit> {
        return try {
            val response = apiService.deleteBonus(bonusId, btCode, companyCode)
            when {
                response.isSuccessful -> Resource.Success(Unit)
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to delete bonus"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ═══════════════════════════════════════════
    // OVERTIME
    // ═══════════════════════════════════════════

    fun getOvertimeList(
        btCode: String, companyCode: String, month: String
    ): Flow<Resource<OvertimeMonthlyDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getOvertimeList(btCode, companyCode, month)
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> emit(Resource.Success(body.data))
                response.isSuccessful -> emit(Resource.Error(body?.message ?: "No data"))
                else -> emit(Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to load overtime report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    suspend fun updateOvertime(
        otId: String,
        btCode: String,
        companyCode: String,
        empCode: String,
        otDate: String,
        otHours: Double,
        otAmount: Double,
        remarks: String
    ): Resource<OvertimeEntryDto> {
        return try {
            val response = apiService.updateOvertime(
                otId,
                OvertimeEntryRequest(
                    btCode      = btCode,
                    companyCode = companyCode,
                    empCode     = empCode,
                    otDate      = otDate,
                    otHours     = otHours,
                    otAmount    = otAmount,
                    remarks     = remarks
                )
            )
            val body = response.body()
            when {
                response.isSuccessful && body?.data != null -> Resource.Success(body.data)
                response.isSuccessful -> Resource.Error(body?.message ?: "No data")
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to update overtime"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    suspend fun deleteOvertime(
        otId: String,
        btCode: String,
        companyCode: String
    ): Resource<Unit> {
        return try {
            val response = apiService.deleteOvertime(otId, btCode, companyCode)
            when {
                response.isSuccessful -> Resource.Success(Unit)
                else -> Resource.Error(
                    parseError(response.errorBody()?.string()) ?: "Failed to delete overtime"
                )
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ═══════════════════════════════════════════
    // HELPER
    // ═══════════════════════════════════════════

    private fun parseError(errorBody: String?): String? {
        return try {
            JSONObject(errorBody ?: return null).optString("message").ifEmpty { null }
        } catch (e: Exception) {
            null
        }
    }
}