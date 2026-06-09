package com.blivtech.emptrack.data.model.parcel

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ShiftParcel(
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val status: Int
) : Parcelable