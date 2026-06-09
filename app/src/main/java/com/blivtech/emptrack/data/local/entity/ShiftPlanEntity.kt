package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_shift_plan")
data class ShiftPlanEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val empCode: String,
    val weekStartDate: String,   // ✅ Monday of that week — yyyy-MM-dd
    val weekEndDate: String,     // ✅ Sunday of that week — yyyy-MM-dd
    val status: Int = 1,
    val createdAt: String = "",
    val updatedAt: String = ""
)