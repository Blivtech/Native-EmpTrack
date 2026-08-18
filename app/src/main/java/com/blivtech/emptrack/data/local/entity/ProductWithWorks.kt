package com.blivtech.emptrack.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation

/** One product with its priced works — what the list and entry screens use. */
data class ProductWithWorks(
    @Embedded val product: ProductEntity,
    @Relation(parentColumn = "id", entityColumn = "productId")
    val works: List<ProductWorkEntity>
)