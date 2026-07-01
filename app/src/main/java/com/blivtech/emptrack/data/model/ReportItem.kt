package com.blivtech.emptrack.data.model

data class ReportItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: String,       // ATTENDANCE / WAGES / WORK
    val tag: String,            // Daily / Weekly / Monthly
    val iconRes: Int,
    val iconBgColor: String,
    val iconTintColor: String,
    val destination: Class<*>   // which Activity to open
)