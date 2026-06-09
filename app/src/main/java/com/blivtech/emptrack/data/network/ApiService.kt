package com.blivtech.emptrack.data.network

import com.blivtech.emptrack.data.model.ApiResponse
import com.blivtech.emptrack.data.model.AttendanceRequest
import com.blivtech.emptrack.data.model.AttendanceResponse
import com.blivtech.emptrack.data.model.LoginRequest
import com.blivtech.emptrack.data.model.LoginResponse
import com.blivtech.emptrack.data.model.RegisterRequest
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.data.model.CompanyData
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.data.model.EmployeeResponse
import com.blivtech.emptrack.data.model.MasterResponse
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.PUT
import retrofit2.http.DELETE
import retrofit2.http.Path

interface ApiService {

    @POST("api/auth/register")
    suspend fun register(
        @Body request: RegisterRequest
    ): Response<ApiResponse<Any>>

    @POST("api/auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<ApiResponse<LoginResponse>>

    @GET("api/master")
    suspend fun getMasterData(
        @Query("btCode") btCode: String
    ): Response<MasterResponse>

    @POST("api/company")
    suspend fun createCompany(
        @Body request: CompanyRequest
    ): Response<ApiResponse<CompanyData>>

    @PUT("api/company/{id}")
    suspend fun updateCompany(
        @Path("id") id: Long,
        @Body request: CompanyRequest
    ): Response<ApiResponse<CompanyData>>

    @DELETE("api/company/{id}")
    suspend fun deleteCompany(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>

    @GET("api/employee")
    suspend fun getEmployees(
        @Query("btCode") btCode: String,
        @Query("companyId") companyId: Long
    ): Response<ApiResponse<List<EmployeeRequest>>>

    @POST("api/employees/save")
    suspend fun createEmployee(
        @Body request: EmployeeRequest
    ): Response<ApiResponse<EmployeeResponse>>

    @PUT("api/employee/{id}")
    suspend fun updateEmployee(
        @Path("id") id: Long,
        @Body request: EmployeeRequest
    ): Response<ApiResponse<EmployeeResponse>>

    @DELETE("api/employee/{id}")
    suspend fun deleteEmployee(
        @Path("id") id: Long
    ): Response<ApiResponse<Any>>

    // ✅ Mark attendance
    @POST("api/attendance/mark")
    suspend fun markAttendance(
        @Body request: AttendanceRequest
    ): Response<ApiResponse<AttendanceResponse>>

    // ✅ Update attendance
    @PUT("api/attendance/update/{attendanceId}")
    suspend fun updateAttendance(
        @Path("attendanceId") attendanceId: String,
        @Body request: AttendanceRequest
    ): Response<ApiResponse<AttendanceResponse>>

    // ✅ Get attendance by ID
    @GET("api/attendance/{attendanceId}")
    suspend fun getAttendanceById(
        @Path("attendanceId") attendanceId: String
    ): Response<ApiResponse<AttendanceResponse>>

    // ✅ Check attendance
    @GET("api/attendance/check")
    suspend fun checkAttendance(
        @Query("btCode") btCode: String,
        @Query("companyId") companyId: String,
        @Query("shiftId") shiftId: String,
        @Query("date") date: String
    ): Response<ApiResponse<Map<String, Any>>>

    // ✅ Today's shifts status
    @GET("api/attendance/today")
    suspend fun getTodayStatus(
        @Query("btCode") btCode: String,
        @Query("companyId") companyId: String
    ): Response<ApiResponse<List<ShiftStatusResponse>>>
}