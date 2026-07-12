package com.blivtech.emptrack.data.model

data class DesignationRequest(
    val id: Long = 0,
    val btCode: String,
    val desgCode: String,     // API expects deptCode
    val name: String,
    val createdAt: String,
    val updatedAt: String,
    val description: String,
    val status: Int = 0
)