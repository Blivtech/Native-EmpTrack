package com.blivtech.emptrack.data.model

/** Server product (add this list to your master-data response as `products`). */
data class ProductDto(
    val id: Long,
    val btCode: String = "",
    val companyCode: String = "",
    val productCode: String,
    val name: String,
    val unit: String,
    val icon: String?,
    val status: Int,
    val works: List<WorkLineDto>
)

data class WorkLineDto(
    val workTypeId: Long,
    val workCode: String?,
    val workName: String,
    val rate: Double
)