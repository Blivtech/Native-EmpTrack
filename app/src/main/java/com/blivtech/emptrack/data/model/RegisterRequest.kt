package com.blivtech.emptrack.data.model

data class RegisterRequest(
    val displayName: String,
    val username: String,
    val phoneNumber: String,
    val whatsappNumber: String? = null,
    val email: String? = null,
    val password: String,
    val userType: Int? = null,
    val referralId: String? = null,
    val address: String? = null,
    val reportTo: Int? = null,
    val fcmToken: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val appVersion: String? = null
)