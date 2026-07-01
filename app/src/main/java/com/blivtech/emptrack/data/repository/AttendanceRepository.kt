package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AttendanceRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ✅ Mark attendance
    suspend fun markAttendance(
        request: AttendanceRequest
    ): Resource<AttendanceResponse> {
        return try {
            val response = apiService.markAttendance(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to mark attendance")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Update attendance
    suspend fun updateAttendance(
        attendanceId: String,
        request: AttendanceRequest
    ): Resource<AttendanceResponse> {
        return try {
            val response = apiService.updateAttendance(attendanceId, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to update attendance")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Get attendance by ID
    suspend fun getAttendanceById(
        attendanceId: String
    ): Resource<AttendanceResponse> {
        return try {
            val response = apiService.getAttendanceById(attendanceId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to get attendance")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Check attendance
    suspend fun checkAttendance(
        btCode: String,
        companyId: String,
        shiftId: String,
        date: String
    ): Resource<Map<String, Any>> {
        return try {
            val response = apiService.checkAttendance(
                btCode, companyId, shiftId, date
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to check attendance")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Today's shifts status
    suspend fun getTodayStatus(
        btCode: String,
        companyId: String, date: String
    ): Resource<List<ShiftStatusResponse>> {
        return try {
            val response = apiService.getTodayStatus(btCode, companyId,date)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to get today status")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }
}