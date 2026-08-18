package com.blivtech.emptrack.data.model

/** Body for create/edit. Scope (btCode/companyCode) rides on query params. */
data class ProductRequestDto(
    val name: String,
    val unit: String,
    val icon: String?,
    val works: List<WorkLineRequestDto>
)

data class WorkLineRequestDto(
    val workName: String,
    val rate: Double
)