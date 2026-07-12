package com.blivtech.emptrack.data.model

data class DesignationResponse(
    val id: Long,
    val btCode: String,
    val desgCode: String,
    val name: String,
    val description: String,
    val status: Int,
    val createdAt: String?,
    val updatedAt: String?
)