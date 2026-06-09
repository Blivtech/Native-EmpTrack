package com.blivtech.emptrack.data.model

data class EmployeeResponse(
    val id: Long,
    val btCode: String,
    val empCode: String,
    val companyCode: String,
    val deptCode: String,
    val desgCode: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val gender: Int?,
    val dob: String?,
    val joiningDate: String?,
    val salaryType: Int?,
    val salaryAmount: Double?,
    val status: Int
)