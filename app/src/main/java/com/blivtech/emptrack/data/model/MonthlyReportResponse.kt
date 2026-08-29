package com.blivtech.emptrack.data.model

data class MonthlyReportResponse(
    val code: Int,
    val message: String,
    val data: MonthlyReportData
)
data class MonthlyReportData(
    val btCode: String,
    val companyCode: String,
    val month: String,               // "2026-08"
    val totalEmployees: Int,
    val totalPresent: Int,
    val totalAbsent: Int,
    val totalHoliday: Int,
    val totalWeekOff: Int,
    val workingDays: Int,
    val employees: List<MonthlyEmployeeSummaryDto>
)
