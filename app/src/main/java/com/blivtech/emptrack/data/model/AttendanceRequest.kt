package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ REQUEST MODELS
// ─────────────────────────────────────

data class AttendanceRequest(
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val attendanceDate: String,
    val markedBy: Long,
    val employees: List<AttendanceDetailRequest>
)

data class AttendanceDetailRequest(
    val empCode: String,
    val dayPlanStatus: Int,       // 1=Working 2=WeekOff 3=Leave 4=Holiday
    val workType: Int,            // 1=Full 2=Half
    val presentCount: Double,
    val absentCount: Int,
    val remarks: String = "",
    val isDeviation: Boolean = false
)

// ─────────────────────────────────────
// ✅ RESPONSE MODELS
// ─────────────────────────────────────

data class AttendanceResponse(
    val attendanceId: String?,
    val companyCode: String,
    val shiftCode: String,
    val attendanceDate: String,
    val totalEmployees: Int?,
    val presentCount: Double?,
    val absentCount: Int?,
    val weekoffCount: Int?,
    val leaveCount: Int?,
    val holidayCount: Int?,
    val isMarked: Boolean,
    val mode: String?,
    val employees: List<AttendanceDetailResponse>?
)

data class AttendanceDetailResponse(
    val detailId: String?,
    val empCode: String,
    val name: String,
    val dayPlanStatus: Int,
    val workType: Int,
    val presentCount: Double,
    val absentCount: Int,
    val remarks: String?,
    val isDeviation: Boolean = false
)

data class ShiftStatusResponse(
    val shiftCode: String,
    val shiftName: String,
    val attendanceId: String?,
    val isMarked: Boolean,
    val presentCount: Double,
    val absentCount: Int,
    val weekoffCount: Int,
    val leaveCount: Int,
    val holidayCount: Int,
    val mode: String
)

data class CalendarDayResponse(
    val date: String,
    val hasMissing: Boolean,
    val shifts: List<CalendarShiftResponse>
)

data class CalendarShiftResponse(
    val shiftCode: String,
    val shiftName: String,
    val isMarked: Boolean,
    val presentCount: Double,
    val status: String            // full/half/missing/off/leave/holiday
)