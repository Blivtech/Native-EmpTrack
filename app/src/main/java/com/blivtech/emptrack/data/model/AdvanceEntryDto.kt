package com.blivtech.emptrack.data.model
// AdvanceMonthlyDto.kt
data class AdvanceMonthlyDto(
    val month: String,
    val totalEntries: Int,
    val totalAmount: Double,
    val entries: List<AdvanceEntryDto>
)

data class AdvanceEntryDto(
    val id: Long,
    val advanceId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val deptName: String,
    val requestDate: String,
    val amount: Double,
    val repayMonth: String,
    val remarks: String,
    val status: Int
)

data class AdvanceEntryRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val requestDate: String,
    val amount: Double,
    val repayMonth: String,
    val remarks: String
)

// BonusMonthlyDto.kt
data class BonusMonthlyDto(
    val month: String,
    val totalEntries: Int,
    val totalAmount: Double,
    val entries: List<BonusEntryDto>
)

data class BonusEntryDto(
    val id: Long,
    val bonusId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val deptName: String,
    val bonusDate: String,
    val bonusType: String,
    val amount: Double,
    val remarks: String,
    val status: Int
)

data class BonusEntryRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val bonusDate: String,
    val bonusType: String,
    val amount: Double,
    val remarks: String
)
// OvertimeMonthlyDto.kt
data class OvertimeMonthlyDto(
    val month: String,
    val totalEntries: Int,
    val totalHours: Double,
    val totalAmount: Double,
    val entries: List<OvertimeEntryDto>
)

data class OvertimeEntryDto(
    val id: Long,
    val otId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val deptName: String,
    val otDate: String,
    val otHours: Double,
    val ratePerHour: Double,
    val otAmount: Double,
    val remarks: String,
    val status: Int
)

data class OvertimeEntryRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val otDate: String,
    val otHours: Double,
    val otAmount: Double,
    val remarks: String
)