package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import org.json.JSONObject
import javax.inject.Inject

class AdvanceRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ─────────────────────────────────
    // ✅ Get advance report
    // ─────────────────────────────────
    fun getAdvanceReport(
        btCode: String,
        companyCode: String,
        month: String
    ): Flow<Resource<AdvanceReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAdvanceReport(
                btCode, companyCode, month
            )
            val body = response.body()
            when {
                response.isSuccessful &&
                body != null  ->
                    emit(Resource.Success(body.data!!))

                response.isSuccessful &&
                body != null ->
                    emit(Resource.Error(body.message))

                else -> emit(Resource.Error(
                    parseErrorMessage(
                        response.errorBody()?.string()
                    ) ?: "Failed to load advance report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(
                e.localizedMessage ?: "Network error"
            ))
        }
    }

    // ─────────────────────────────────
    // ✅ Get employee advance detail
    // ─────────────────────────────────
    fun getAdvanceEmployeeDetail(
        btCode: String,
        companyCode: String,
        month: String,
        empCode: String
    ): Flow<Resource<AdvanceEmployeeDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getAdvanceEmployeeDetail(
                btCode, companyCode, month, empCode
            )
            val body = response.body()
            when {
                response.isSuccessful &&
                body != null-> {
                    val dto = body.data!!
                    emit(Resource.Success(
                        AdvanceEmployeeDetail(
                            empCode     = dto.empCode,
                            empName     = dto.empName,
                            deptName    = dto.deptName,
                            desgName    = dto.desgName,
                            shiftName   = dto.shiftName,
                            month       = dto.month,
                            totalAmount = dto.totalAmount,
                            entryCount  = dto.entryCount,
                            entries     = dto.entries.map { e ->
                                AdvanceEntry(
                                    id         = e.id,
                                    date       = e.date,
                                    amount     = e.amount,
                                    reason     = e.reason,
                                    approvedBy = e.approvedBy
                                )
                            }
                        )
                    ))
                }
                response.isSuccessful &&
                body != null ->
                    emit(Resource.Error(body.message))

                else -> emit(Resource.Error(
                    parseErrorMessage(
                        response.errorBody()?.string()
                    ) ?: "Failed to load detail"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(
                e.localizedMessage ?: "Network error"
            ))
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            JSONObject(errorBody ?: return null)
                .optString("message").ifEmpty { null }
        } catch (e: Exception) { null }
    }
}