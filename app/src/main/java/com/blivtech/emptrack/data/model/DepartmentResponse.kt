package com.blivtech.emptrack.data.model

data class DepartmentResponse(
    val id: Long,
    val btCode: String,
    val deptCode: String,
    val name: String,
    val description: String,
    val status: Int,
    val createdAt: String?,
    val updatedAt: String?
)