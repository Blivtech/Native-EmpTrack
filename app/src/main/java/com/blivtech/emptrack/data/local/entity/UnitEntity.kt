package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// ✅ Local only — NOT synced to server
@Entity(tableName = "tbl_units")
data class UnitEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val btCode: String,
    val unitName: String,   // kg, mtr, bundle, pcs
    val createdAt: String
)