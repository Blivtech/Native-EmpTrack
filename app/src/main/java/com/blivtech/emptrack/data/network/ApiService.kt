package com.blivtech.emptrack.data.network

import com.blivtech.emptrack.data.model.AdvanceRequest
import com.blivtech.emptrack.data.model.AdvanceResponse
import com.blivtech.emptrack.data.model.ApiResponse
import com.blivtech.emptrack.data.model.AttendanceEmployeeDto
import com.blivtech.emptrack.data.model.AttendanceRequest
import com.blivtech.emptrack.data.model.AttendanceResponse
import com.blivtech.emptrack.data.model.BonusRequest
import com.blivtech.emptrack.data.model.BonusResponse
import com.blivtech.emptrack.data.model.LoginRequest
import com.blivtech.emptrack.data.model.LoginResponse
import com.blivtech.emptrack.data.model.RegisterRequest
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.data.model.CompanyData
import com.blivtech.emptrack.data.model.ContractEntryRequest
import com.blivtech.emptrack.data.model.ContractProductRequest
import com.blivtech.emptrack.data.model.DailyReportSummaryDto
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.data.model.EmployeeResponse
import com.blivtech.emptrack.data.model.EmployeeWeeklyDetailDto
import com.blivtech.emptrack.data.model.MasterResponse
import com.blivtech.emptrack.data.model.OvertimeRequest
import com.blivtech.emptrack.data.model.OvertimeResponse
import com.blivtech.emptrack.data.model.ProductRequest
import com.blivtech.emptrack.data.model.ProductResponse
import com.blivtech.emptrack.data.model.ShiftStatusResponse
import com.blivtech.emptrack.data.model.WeeklyReportDto
import com.blivtech.emptrack.data.model.WeeklyShiftEmployeeDto
import com.blivtech.emptrack.data.model.WorkEntryRequest
import com.blivtech.emptrack.data.model.WorkEntryResponse
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

    @POST("api/companies/save")
    suspend fun createCompany(
        @Body request: CompanyRequest
    ): Response<ApiResponse<CompanyData>>

    @PUT("api/companies/{id}")
    suspend fun updateCompany(
        @Path("companyCode") companyCode: String,
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


    @PUT("api/employees/{empCode}/{companyCode}")
    suspend fun updateEmployee(
        @Path("empCode")     empCode: String,
        @Path("companyCode") companyCode: String,
        @Body request: EmployeeRequest
    ): Response<ApiResponse<EmployeeResponse>>


    @DELETE("api/employees/{empCode}")
    suspend fun deleteEmployee(
        @Path("empCode")      empCode: String,
        @Query("companyCode") companyCode: String
    ): Response<ApiResponse<Any>>


    @POST("api/attendance/mark")
    suspend fun markAttendance(
        @Body request: AttendanceRequest
    ): Response<ApiResponse<AttendanceResponse>>

    @PUT("api/attendance/update/{attendanceId}")
    suspend fun updateAttendance(
        @Path("attendanceId") attendanceId: String,
        @Body request: AttendanceRequest
    ): Response<ApiResponse<AttendanceResponse>>

    @GET("api/attendance/{attendanceId}")
    suspend fun getAttendanceById(
        @Path("attendanceId") attendanceId: String
    ): Response<ApiResponse<AttendanceResponse>>


    @GET("api/attendance/check")
    suspend fun checkAttendance(
        @Query("btCode") btCode: String,
        @Query("companyId") companyId: String,
        @Query("shiftId") shiftId: String,
        @Query("date") date: String
    ): Response<ApiResponse<Map<String, Any>>>

    @GET("api/attendance/today")
    suspend fun getTodayStatus(
        @Query("btCode") btCode: String,
        @Query("companyId") companyId: String
    ): Response<ApiResponse<List<ShiftStatusResponse>>>


    @POST("api/overtime/add")
    suspend fun addOvertime(
        @Body request: OvertimeRequest
    ): Response<ApiResponse<String>>

    @POST("api/advance/add")
    suspend fun addAdvance(
        @Body request: AdvanceRequest
    ): Response<ApiResponse<String>>

    @POST("api/bonus/add")
    suspend fun addBonus(
        @Body request: BonusRequest
    ): Response<ApiResponse<String>>


    // ─── Products ───────────────────────────────

    @GET("api/products")
    suspend fun getProducts(
        @Query("btCode") btCode: String,
        @Query("companyCode") companyCode: String
    ): Response<ApiResponse<List<ProductResponse>>>

    @POST("api/products/add")
    suspend fun addProduct(
        @Body request: ProductRequest
    ): Response<ApiResponse<String>>

    @PUT("api/products/update/{productId}")
    suspend fun updateProduct(
        @Path("productId") productId: String,
        @Body request: ProductRequest
    ): Response<ApiResponse<String>>

    @DELETE("api/products/{productId}")
    suspend fun deleteProduct(
        @Path("productId") productId: String
    ): Response<ApiResponse<String>>

// ─── Work Entries ────────────────────────────

    @POST("api/work/add")
    suspend fun addWorkEntry(
        @Body request: WorkEntryRequest
    ): Response<ApiResponse<String>>

    @GET("api/work")
    suspend fun getWorkEntries(
        @Query("btCode") btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("date") date: String? = null,
        @Query("month") month: String? = null
    ): Response<ApiResponse<List<WorkEntryResponse>>>

    // ✅ Contract Wage APIs
    @POST("api/contract-products/add")
    suspend fun addContractProduct(
        @Body request: ContractProductRequest
    ): Response<ApiResponse<Any>>

    @POST("api/contract-entries/add")
    suspend fun addContractEntries(
        @Body request: ContractEntryRequest
    ): Response<ApiResponse<Any>>

    @GET("api/contract-entries")
    suspend fun getContractEntries(
        @Query("btCode") btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("month") month: String
    ): Response<ApiResponse<List<Any>>>

    // ✅ Daily report APIs
    @GET("api/reports/attendance/daily")
    suspend fun getDailyReport(
        @Query("btCode")      btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("date")        date: String
    ): Response<ApiResponse<DailyReportSummaryDto>>

    @GET("api/reports/attendance/daily/employees")
    suspend fun getShiftEmployees(
        @Query("btCode")      btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("date")        date: String,
        @Query("shiftCode")   shiftCode: String,
        @Query("type")        type: String   // PRESENT or LEAVE
    ): Response<ApiResponse<List<AttendanceEmployeeDto>>>


    // ✅ Weekly Report APIs
    @GET("api/reports/attendance/weekly")
    suspend fun getWeeklyReport(
        @Query("btCode")      btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("weekStart")   weekStart: String,
        @Query("weekEnd")     weekEnd: String
    ): Response<ApiResponse<WeeklyReportDto>>

    @GET("api/reports/attendance/weekly/employees")
    suspend fun getWeeklyShiftEmployees(
        @Query("btCode")      btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("weekStart")   weekStart: String,
        @Query("weekEnd")     weekEnd: String,
        @Query("shiftCode")   shiftCode: String,
        @Query("type")        type: String
    ): Response<ApiResponse<List<WeeklyShiftEmployeeDto>>>

    @GET("api/reports/attendance/weekly/employee-detail")
    suspend fun getEmployeeWeeklyDetail(
        @Query("btCode")      btCode: String,
        @Query("companyCode") companyCode: String,
        @Query("weekStart")   weekStart: String,
        @Query("weekEnd")     weekEnd: String,
        @Query("shiftCode")   shiftCode: String,
        @Query("empCode")     empCode: String
    ): Response<ApiResponse<EmployeeWeeklyDetailDto>>
}