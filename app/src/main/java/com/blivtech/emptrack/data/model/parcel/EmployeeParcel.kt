package com.blivtech.emptrack.data.model.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class EmployeeParcel(
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
    val profileImage: String?,
    val salaryType: Int?,
    val salaryAmount: Double?,
    val lastAppraisalDate: String?,
    val status: Int = 1
) : Parcelable