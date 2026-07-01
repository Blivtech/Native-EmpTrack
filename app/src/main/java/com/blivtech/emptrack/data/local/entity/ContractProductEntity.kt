package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_contract_products")
data class ContractProductEntity(
    @PrimaryKey
    val productId: String,
    val btCode: String,
    val companyCode: String,
    val productName: String,
    val workName: String,
    val ratePerUnit: Double,
    val unit: String,
    val colorTag: String = "#1565C0",
    val status: Int = 1
)