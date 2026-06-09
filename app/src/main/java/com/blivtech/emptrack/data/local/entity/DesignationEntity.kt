package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_designations")
data class DesignationEntity(
    @PrimaryKey
    val id: Long,
    val btCode: String,
    val desgCode: String,
    val name: String,
    val description: String?,
    val status: Int
)