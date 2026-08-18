package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "product_works",
    indices = [Index("productId"), Index("btCode"), Index("companyCode")]
)
data class ProductWorkEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val productId: Long,             // = ProductEntity.id
    val btCode: String,
    val companyCode: String,
    val workTypeId: Long,
    val workCode: String?,
    val workName: String,
    val rate: Double                 // ₹ per piece
)