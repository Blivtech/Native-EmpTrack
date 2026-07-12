package com.blivtech.emptrack.data.model

data class DepartmentRequest(
    val id: Long = 0,          // server assigns
    val btCode: String,
    val deptCode: String,
    val name: String,
    val description: String,
    val status: Int = 1,   val createdAt: String?,
    val updatedAt: String?
)
