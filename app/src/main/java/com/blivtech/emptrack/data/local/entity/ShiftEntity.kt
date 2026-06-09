package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_shifts")
data class ShiftEntity(
    @PrimaryKey
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val status: Int
)