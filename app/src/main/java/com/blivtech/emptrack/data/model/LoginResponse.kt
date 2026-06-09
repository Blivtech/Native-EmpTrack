package com.blivtech.emptrack.data.model

data class LoginResponse(
    val id: Long,
    val btCode: String,
    val displayName: String,
    val phoneNumber: String,
    val email: String?,
    val userType: Int,
    val token: String
)