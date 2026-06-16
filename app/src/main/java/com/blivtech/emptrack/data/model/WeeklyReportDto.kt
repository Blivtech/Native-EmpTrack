package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ Daily status — one entry per day
// ─────────────────────────────────────
data class DailyStatus(
    val date: String,
    val dayName: String,
    val status: String,
    val statusLabel: String
)

// ─────────────────────────────────────
// ✅ Daily status DTO (API response)
// ─────────────────────────────────────
data class DailyStatusDto(
    val date: String,
    val dayName: String,
    val status: String,
    val statusLabel: String
)

// ─────────────────────────────────────
// ✅ Employee weekly detail DTO (API)
// ─────────────────────────────────────
data class EmployeeWeeklyDetailDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val designation: String,
    val shiftCode: String,
    val shiftName: String,
    val weekStart: String,
    val weekEnd: String,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val totalDays: Int,
    val attendancePercent: Int,
    val dailyStatus: List<DailyStatusDto>
)

// ─────────────────────────────────────
// ✅ Employee weekly detail UI model
// ─────────────────────────────────────
data class EmployeeWeeklyDetail(
    val empCode: String,
    val empName: String,
    val department: String,
    val shiftName: String,
    val weekStart: String,
    val weekEnd: String,
    val presentDays: Int,
    val absentDays: Int,
    val lateDays: Int,           // ✅ Added
    val holidayDays: Int,        // ✅ Added
    val weekOffDays: Int,        // ✅ Added
    val totalDays: Int,
    val attendancePercent: Int,
    val dailyStatus: List<DailyStatus>
)



// ─────────────────────────────────────
// ✅ Weekly report DTO (API)
// ─────────────────────────────────────
data class WeeklyReportDto(
    val btCode: String,
    val companyCode: String,
    val weekStart: String,
    val weekEnd: String,
    val totalPresent: Int,
    val totalAbsent: Int,
    val workDays: Int,
    val shifts: List<WeeklyShiftSummaryDto>
)

data class WeeklyShiftSummaryDto(
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val submittedDays: Int,
    val totalDays: Int
)

data class WeeklyShiftEmployeeDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val designation: String,
    val presentDays: Int,
    val absentDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)

// ─────────────────────────────────────
// ✅ Weekly report UI models
// ─────────────────────────────────────
data class WeeklyShiftSummary(
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val submittedDays: Int,
    val totalDays: Int
)

data class WeeklyShiftEmployee(
    val empCode: String,
    val empName: String,
    val department: String,
    val presentDays: Int,
    val absentDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)