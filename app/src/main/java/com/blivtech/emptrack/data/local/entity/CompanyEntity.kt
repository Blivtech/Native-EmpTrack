package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_companies")
data class CompanyEntity(
    @PrimaryKey
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
)