package com.blivtech.emptrack.data.model

data class CardDetailsForHomeActivity(
    val id: String,
    val iconRes: Int,           // drawable resource for the icon
    val bgColorRes: Int,        // background color/drawable for icon circle/rounded box
    val cardName: String,       // "Attendance"
    val subtitle: String,       // "Day plan · Shifts"
    val needFlag: Boolean = true // e.g. show badge/notification dot, or enable/disable card
)
