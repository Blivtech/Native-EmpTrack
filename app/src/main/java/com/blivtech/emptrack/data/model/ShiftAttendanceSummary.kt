package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ Daily report — shift summary
// ─────────────────────────────────────
data class ShiftAttendanceSummary(
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val attendanceDate: String,
    val submittedAt: String?,       // null = not submitted
    val presentCount: Int,
    val leaveCount: Int,
    val totalCount: Int
)

// ─────────────────────────────────────
// ✅ Employee attendance item
// ─────────────────────────────────────
data class AttendanceEmployeeItem(
    val empCode: String,
    val empName: String,
    val department: String,
    val status: String,             // P / A / L / H / WO
    val statusLabel: String,        // Present / Absent / Late / Holiday
    val lateMinutes: Int = 0        // for Late status
)