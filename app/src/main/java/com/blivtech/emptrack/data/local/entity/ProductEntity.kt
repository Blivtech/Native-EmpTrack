package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "products",
    indices = [Index("btCode"), Index("companyCode")]
)
data class ProductEntity(
    @PrimaryKey val id: Long,        // server product id
    val btCode: String,
    val companyCode: String,
    val productCode: String,
    val name: String,
    val unit: String,
    val icon: String?,
    val status: Int
)