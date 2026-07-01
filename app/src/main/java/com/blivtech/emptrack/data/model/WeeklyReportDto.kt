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
// ✅ Weekly Overall Report
// ─────────────────────────────────────
data class WeeklyOverallReportDto(
    val btCode: String,
    val companyCode: String,
    val weekStart: String,
    val weekEnd: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workDays: Int,
    val employees: List<WeeklyEmployeeSummaryDto>
)

data class WeeklyEmployeeSummaryDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val deptName: String,
    val desgName: String,
    val shiftCode: String,
    val shiftName: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val lateDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)

// ─────────────────────────────────────
// ✅ Weekly Shift Report
// ─────────────────────────────────────
data class WeeklyShiftReportDto(
    val shiftCode: String,
    val shiftName: String,
    val weekStart: String,
    val weekEnd: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workDays: Int,
    val employees: List<WeeklyEmployeeSummaryDto>
)



// ✅ Must be in ReportModels.kt
data class WeeklyShiftEmployee(
    val empCode: String,
    val empName: String,
    val department: String,
    val deptName: String,
    val desgName: String,
    val shiftCode: String,
    val shiftName: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val lateDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)

// ─────────────────────────────────────
// ✅ Monthly Report DTOs
// ─────────────────────────────────────

data class MonthlyReportDto(
    val btCode: String,
    val companyCode: String,
    val month: String,              // "2026-06"
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workingDays: Int,
    val employees: List<MonthlyEmployeeSummaryDto>
)

data class MonthlyEmployeeSummaryDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val designation: String,
    val deptName: String,
    val desgName: String,
    val shiftCode: String,
    val shiftName: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)

data class MonthlyShiftReportDto(
    val shiftCode: String,
    val shiftName: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workingDays: Int,
    val employees: List<MonthlyEmployeeSummaryDto>
)

data class MonthlyEmployeeDetailDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val designation: String,
    val deptName: String,
    val desgName: String,
    val shiftCode: String,
    val shiftName: String,
    val month: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val totalDays: Int,
    val attendancePercent: Int,
    val presentDates: List<String>,
    val absentDates: List<String>,
    val holidayDates: List<String>,
    val weekOffDates: List<String>
)

// ─────────────────────────────────────
// ✅ Monthly UI Models
// ─────────────────────────────────────

data class MonthlyEmployeeSummary(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val shiftCode: String,
    val shiftName: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val totalDays: Int,
    val attendancePercent: Int
)

data class MonthlyEmployeeDetail(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val shiftName: String,
    val month: String,
    val presentDays: Int,
    val absentDays: Int,
    val holidayDays: Int,
    val weekOffDays: Int,
    val totalDays: Int,
    val attendancePercent: Int,
    val presentDates: List<String>,
    val absentDates: List<String>,
    val holidayDates: List<String>,
    val weekOffDates: List<String>
)

data class MonthlyShiftSummary(
    val shiftCode: String,
    val shiftName: String,
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workingDays: Int,
    val employees: List<MonthlyEmployeeSummary>
)