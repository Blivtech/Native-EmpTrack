package com.blivtech.emptrack.data.model

data class LoginRequest(
    val phoneNumber: String,
    val password: String,
    val fcmToken: String? = null,
    val deviceId: String? = null,
    val deviceName: String? = null,
    val appVersion: String? = null
)