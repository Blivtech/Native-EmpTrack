package com.blivtech.emptrack.data.model

data class EmployeeRequest(
    val btCode: String,
    val empCode: String,
    val companyId: Long,
    val departmentId: Long,
    val designationId: Long,
    val name: String,
    val email: String?,
    val phone: String?,
    val gender: Int?,
    val dob: String?,
    val joiningDate: String?,
    val salaryType: Int?,
    val salaryAmount: Double?,
    val status: Int = 1
)