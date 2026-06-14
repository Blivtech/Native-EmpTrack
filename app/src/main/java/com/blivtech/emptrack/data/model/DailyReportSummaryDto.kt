package com.blivtech.emptrack.data.model

// ✅ API response DTO — daily summary
data class DailyReportSummaryDto(
    val btCode: String,
    val companyCode: String,
    val attendanceDate: String,
    val totalPresent: Int,
    val totalLeave: Int,
    val totalEmployees: Int,
    val submittedShifts: Int,
    val pendingShifts: Int,
    val shifts: List<ShiftSummaryDto>
)

data class ShiftSummaryDto(
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val attendanceDate: String,
    val submittedAt: String?,
    val presentCount: Int,
    val leaveCount: Int,
    val totalCount: Int
)

// ✅ API response DTO — employee
data class AttendanceEmployeeDto(
    val empCode: String,
    val empName: String,
    val department: String,
    val designation: String,
    val status: String,
    val statusLabel: String,
    val lateMinutes: Int
)