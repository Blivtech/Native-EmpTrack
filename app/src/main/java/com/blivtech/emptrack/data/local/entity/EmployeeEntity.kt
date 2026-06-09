package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_employees")
data class EmployeeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val btCode: String,
    val empCode: String,
    val companyCode: String,
    val deptCode: String,
    val desgCode: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val gender: Int?,           // 1=Male 2=Female 3=Other
    val dob: String?,
    val joiningDate: String?,
    val profileImage: String?,
    val salaryType: Int?,       // 1=Daily 2=Weekly 3=Monthly
    val salaryAmount: Double?,
    val lastAppraisalDate: String?,
    val status: Int = 1
)