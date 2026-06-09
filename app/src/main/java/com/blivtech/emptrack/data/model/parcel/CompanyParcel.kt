package com.blivtech.emptrack.data.model.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CompanyParcel(
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val phone: String?,
    val email: String?,
    val logo: String?,
    val status: Int
) : Parcelable