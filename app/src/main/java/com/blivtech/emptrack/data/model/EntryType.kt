package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ Entry type
// ─────────────────────────────────────
enum class EntryType { OVERTIME, ADVANCE, BONUS }

enum class EntryMode { SINGLE, MULTIPLE }

// ─────────────────────────────────────
// ✅ Request models
// ─────────────────────────────────────
data class OvertimeRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val otDate: String,
    val shiftCode: String,
    val otHours: Double,
    val otAmount: Double,
    val remarks: String = "",
    val status: Int = 1          // 1=Active 2=Paid
)

data class AdvanceRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val requestDate: String,
    val amount: Double,
    val repayMonth: String,      // 2026-07
    val remarks: String = "",
    val status: Int = 1          // 1=Active 2=Recovered
)

data class BonusRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val bonusDate: String,
    val bonusType: String,       // Performance/Festival/Incentive/Special/Other
    val amount: Double,
    val remarks: String = "",
    val status: Int = 1          // 1=Active 2=Paid
)

// ─────────────────────────────────────
// ✅ Response models
// ─────────────────────────────────────
data class OvertimeResponse(
    val otId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val otDate: String,
    val shiftCode: String,
    val shiftName: String,
    val otHours: Double,
    val otAmount: Double,
    val remarks: String?,
    val status: Int
)

data class AdvanceResponse(
    val advanceId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val requestDate: String,
    val amount: Double,
    val repayMonth: String,
    val remarks: String?,
    val status: Int
)

data class BonusResponse(
    val bonusId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val bonusDate: String,
    val bonusType: String,
    val amount: Double,
    val remarks: String?,
    val status: Int
)