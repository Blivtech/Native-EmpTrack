package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.AttendanceEmployeeItem
import com.blivtech.emptrack.data.model.DailyStatus
import com.blivtech.emptrack.data.model.EmployeeWeeklyDetail
import com.blivtech.emptrack.data.model.MonthlyEmployeeDetail
import com.blivtech.emptrack.data.model.MonthlyReportDto
import com.blivtech.emptrack.data.model.MonthlyShiftReportDto
import com.blivtech.emptrack.data.model.ShiftAttendanceSummary
import com.blivtech.emptrack.data.model.WeeklyOverallReportDto
import com.blivtech.emptrack.data.model.WeeklyShiftReportDto
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.CommonClass.parseErrorMessage
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService
) {


    fun getShiftSummaries(
        btCode: String,
        companyCode: String,
        date: String
    ): Flow<Resource<List<ShiftAttendanceSummary>>> = flow {

        emit(Resource.Loading)

        try {
            val response = apiService.getDailyReport(
                btCode      = btCode,
                companyCode = companyCode,
                date        = date
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // ✅ Map API DTO → UI model
                val summaries = body.data?.shifts?.map { dto ->
                    ShiftAttendanceSummary(
                        shiftCode      = dto.shiftCode,
                        shiftName      = dto.shiftName,
                        startTime      = dto.startTime,
                        endTime        = dto.endTime,
                        attendanceDate = dto.attendanceDate,
                        submittedAt    = dto.submittedAt,
                        presentCount   = dto.presentCount,
                        leaveCount     = dto.leaveCount,
                        weekOffCount     = dto.weekOffCount,
                        holidayCount     = dto.holidayCount,
                        totalCount     = dto.totalCount
                    )
                } ?: emptyList()

                emit(Resource.Success(summaries))

            } else {
                emit(Resource.Error(
                    response.body()?.message ?: "Failed to load report"
                ))
            }

        } catch (e: Exception) {
            emit(Resource.Error(
                e.localizedMessage ?: "Network error"
            ))
        }
    }

    // ─────────────────────────────────
    // ✅ Get employees for shift + type
    //    Calls → GET /api/reports/attendance/daily/employees
    // ─────────────────────────────────
    fun getEmployeesByShiftAndType(
        btCode: String,
        companyCode: String,
        date: String,
        shiftCode: String,
        type: String        // "PRESENT" or "LEAVE"
    ): Flow<Resource<List<AttendanceEmployeeItem>>> = flow {

        emit(Resource.Loading)

        try {
            val response = apiService.getShiftEmployees(
                btCode      = btCode,
                companyCode = companyCode,
                date        = date,
                shiftCode   = shiftCode,
                type        = type
            )

            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!

                // ✅ Map API DTO → UI model
                val employees = body.data?.map { dto ->
                    AttendanceEmployeeItem(
                        empCode     = dto.empCode,
                        empName     = dto.empName,
                        desgCode  = dto.desgCode,
                        desgName  = dto.desgName,
                        deptCode  = dto.deptCode,
                        deptName  = dto.deptName,
                        status      = dto.status,
                        statusLabel = dto.statusLabel,
                        lateMinutes = dto.lateMinutes
                    )
                } ?: emptyList()

                emit(Resource.Success(employees))

            } else {
                emit(Resource.Error(
                    response.body()?.message ?: "Failed to load employees"
                ))
            }

        } catch (e: Exception) {
            emit(Resource.Error(
                e.localizedMessage ?: "Network error"
            ))
        }
    }

    // ✅ Weekly overall
    fun getWeeklyOverallReport(
        btCode: String, companyCode: String,
        weekStart: String, weekEnd: String
    ): Flow<Resource<WeeklyOverallReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWeeklyOverallReport(
                btCode, companyCode, weekStart, weekEnd
            )
            val body = response.body()
            when {
                response.isSuccessful &&
                        body != null  ->
                    emit(Resource.Success(body.data!!))
                response.isSuccessful && body != null ->
                    emit(Resource.Error(body.message))
                else -> emit(Resource.Error(
                    parseErrorMessage(
                        response.errorBody()?.string()
                    ) ?: "Failed to load weekly report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ✅ Weekly shift wise
    fun getWeeklyShiftReport(
        btCode: String, companyCode: String,
        weekStart: String, weekEnd: String,
        shiftCode: String
    ): Flow<Resource<WeeklyShiftReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWeeklyShiftReport(
                btCode, companyCode, weekStart, weekEnd, shiftCode
            )
            val body = response.body()
            when {
                response.isSuccessful &&
                        body != null  ->
                    emit(Resource.Success(body.data!!))
                response.isSuccessful && body != null ->
                    emit(Resource.Error(body.message))
                else -> emit(Resource.Error(
                    parseErrorMessage(
                        response.errorBody()?.string()
                    ) ?: "Failed to load shift report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ✅ Map DailyStatusDto → DailyStatus (UI model)
    fun getEmployeeWeeklyDetail(
        btCode: String, companyCode: String,
        weekStart: String, weekEnd: String,
        shiftCode: String, empCode: String
    ): Flow<Resource<EmployeeWeeklyDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getEmployeeWeeklyDetail(
                btCode, companyCode, weekStart, weekEnd, shiftCode, empCode
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val dto = response.body()!!.data!!
                emit(Resource.Success(
                    EmployeeWeeklyDetail(
                        empCode           = dto.empCode,
                        empName           = dto.empName,
                        department        = dto.department,
                        shiftName         = dto.shiftName,
                        weekStart         = dto.weekStart,
                        weekEnd           = dto.weekEnd,
                        presentDays       = dto.presentDays,
                        absentDays        = dto.absentDays,
                        lateDays          = dto.lateDays,
                        holidayDays       = dto.holidayDays,
                        weekOffDays       = dto.weekOffDays,
                        totalDays         = dto.totalDays,
                        attendancePercent = dto.attendancePercent,
                        // ✅ Map DailyStatusDto → DailyStatus
                        dailyStatus = dto.dailyStatus.map { d ->
                            DailyStatus(
                                date        = d.date,
                                dayName     = d.dayName,
                                status      = d.status,
                                statusLabel = d.statusLabel
                            )
                        }
                    )
                ))
            } else {
                emit(Resource.Error(
                    response.body()?.message ?: "Failed to load detail"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }


    // ✅ Monthly overall
    fun getMonthlyReport(
        btCode: String, companyCode: String, month: String
    ): Flow<Resource<MonthlyReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMonthlyReport(
                btCode, companyCode, month
            )
            val body = response.body()
            when {
                response.isSuccessful && body != null  ->
                    emit(Resource.Success(body.data!!))
                response.isSuccessful && body != null ->
                    emit(Resource.Error(body.message))
                else -> emit(Resource.Error(
                    parseErrorMessage(response.errorBody()?.string())
                        ?: "Failed to load monthly report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ✅ Monthly shift wise
    fun getMonthlyShiftReport(
        btCode: String, companyCode: String,
        month: String, shiftCode: String
    ): Flow<Resource<MonthlyShiftReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMonthlyShiftReport(
                btCode, companyCode, month, shiftCode
            )
            val body = response.body()
            when {
                response.isSuccessful && body != null  ->
                    emit(Resource.Success(body.data!!))
                response.isSuccessful && body != null ->
                    emit(Resource.Error(body.message))
                else -> emit(Resource.Error(
                    parseErrorMessage(response.errorBody()?.string())
                        ?: "Failed to load shift report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ✅ Monthly employee detail
    fun getMonthlyEmployeeDetail(
        btCode: String, companyCode: String,
        month: String, shiftCode: String, empCode: String
    ): Flow<Resource<MonthlyEmployeeDetail>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getMonthlyEmployeeDetail(
                btCode, companyCode, month, shiftCode, empCode
            )
            val body = response.body()
            when {
                response.isSuccessful && body != null  -> {
                    val dto = body.data!!
                    emit(Resource.Success(
                        MonthlyEmployeeDetail(
                            empCode           = dto.empCode,
                            empName           = dto.empName,
                            deptName          = dto.deptName,
                            desgName          = dto.desgName,
                            shiftName         = dto.shiftName,
                            month             = dto.month,
                            presentDays       = dto.presentDays,
                            absentDays        = dto.absentDays,
                            holidayDays       = dto.holidayDays,
                            weekOffDays       = dto.weekOffDays,
                            totalDays         = dto.totalDays,
                            attendancePercent = dto.attendancePercent,
                            presentDates      = dto.presentDates,
                            absentDates       = dto.absentDates,
                            holidayDates      = dto.holidayDates,
                            weekOffDates      = dto.weekOffDates
                        )
                    ))
                }
                response.isSuccessful && body != null ->
                    emit(Resource.Error(body.message))
                else -> emit(Resource.Error(
                    parseErrorMessage(response.errorBody()?.string())
                        ?: "Failed to load employee detail"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }
}