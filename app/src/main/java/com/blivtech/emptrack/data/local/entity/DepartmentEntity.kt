package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_departments")
data class DepartmentEntity(
    @PrimaryKey
    val id: Long,
    val btCode: String,
    val deptCode: String,
    val name: String,
    val description: String?,
    val status: Int
)