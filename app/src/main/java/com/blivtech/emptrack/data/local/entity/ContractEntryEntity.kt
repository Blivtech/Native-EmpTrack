package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_contract_entries")
data class ContractEntryEntity(
    @PrimaryKey
    val entryId: String,
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val shiftName: String,
    val entryDate: String,          // yyyy-MM-dd
    val productId: String,
    val productName: String,
    val workName: String,
    val quantityDone: Double,
    val ratePerUnit: Double,        // snapshot
    val totalAmount: Double,
    val unit: String,
    val status: Int = 1,
    val createdAt: String
)