package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.AttendanceEmployeeItem
import com.blivtech.emptrack.data.model.DailyStatus
import com.blivtech.emptrack.data.model.EmployeeWeeklyDetail
import com.blivtech.emptrack.data.model.ShiftAttendanceSummary
import com.blivtech.emptrack.data.model.WeeklyReportDto
import com.blivtech.emptrack.data.model.WeeklyShiftEmployee
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ReportRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ─────────────────────────────────
    // ✅ Get shift summaries for a date
    //    Calls → GET /api/reports/attendance/daily
    // ─────────────────────────────────
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
                        department  = dto.department,
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

    // ─────────────────────────────────
// ✅ Weekly summary
// ─────────────────────────────────
    fun getWeeklySummary(
        btCode: String,
        companyCode: String,
        weekStart: String,
        weekEnd: String
    ): Flow<Resource<WeeklyReportDto>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWeeklyReport(
                btCode, companyCode, weekStart, weekEnd
            )
            if (response.isSuccessful && response.body()?.data != null) {
                emit(Resource.Success(response.body()!!.data!!))
            } else {
                emit(Resource.Error(
                    response.body()?.message ?: "Failed to load weekly report"
                ))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Network error"))
        }
    }

    // ─────────────────────────────────
// ✅ Weekly shift employees
// ─────────────────────────────────
    fun getWeeklyShiftEmployees(
        btCode: String,
        companyCode: String,
        weekStart: String,
        weekEnd: String,
        shiftCode: String,
        type: String
    ): Flow<Resource<List<WeeklyShiftEmployee>>> = flow {
        emit(Resource.Loading)
        try {
            val response = apiService.getWeeklyShiftEmployees(
                btCode, companyCode, weekStart, weekEnd, shiftCode, type
            )
            if (response.isSuccessful && response.body()?.data != null) {
                val list = response.body()!!.data!!.map { dto ->
                    WeeklyShiftEmployee(
                        empCode            = dto.empCode,
                        empName            = dto.empName,
                        department         = dto.department,
                        presentDays        = dto.presentDays,
                        absentDays         = dto.absentDays,
                        totalDays          = dto.totalDays,
                        attendancePercent  = dto.attendancePercent
                    )
                }
                emit(Resource.Success(list))
            } else {
                emit(Resource.Error(
                    response.body()?.message ?: "Failed to load employees"
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
}