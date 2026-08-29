package com.blivtech.emptrack.data.model

data class CompanyRequest(
    val btCode: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val phone: String?,
    val email: String?,
    val logo: String?,
    val shifts: List<ShiftRequest>
) {
    data class ShiftRequest(
        val shiftName: String,
        val startTime: String,
        val endTime: String,
        val shiftCode: String,
    )
}