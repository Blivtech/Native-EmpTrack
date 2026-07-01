package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ Advance Report DTOs
// ─────────────────────────────────────

data class AdvanceReportDto(
    val btCode: String,
    val companyCode: String,
    val month: String,
    val totalEmployees: Int,
    val totalAmount: Double,
    val entries: List<AdvanceEmployeeDto>
)

data class AdvanceEmployeeDto(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val totalAmount: Double,
    val entryCount: Int,
    val lastDate: String
)

data class AdvanceEmployeeDetailDto(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val shiftName: String,
    val month: String,
    val totalAmount: Double,
    val entryCount: Int,
    val entries: List<AdvanceReportEntryDto>
)

data class AdvanceReportEntryDto(
    val id: Long,
    val date: String,
    val amount: Double,
    val reason: String,
    val approvedBy: String
)

// ─────────────────────────────────────
// ✅ UI Models
// ─────────────────────────────────────

data class AdvanceEmployee(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val totalAmount: Double,
    val entryCount: Int,
    val lastDate: String
)

data class AdvanceEmployeeDetail(
    val empCode: String,
    val empName: String,
    val deptName: String,
    val desgName: String,
    val shiftName: String,
    val month: String,
    val totalAmount: Double,
    val entryCount: Int,
    val entries: List<AdvanceEntry>
)

data class AdvanceEntry(
    val id: Long,
    val date: String,
    val amount: Double,
    val reason: String,
    val approvedBy: String
)