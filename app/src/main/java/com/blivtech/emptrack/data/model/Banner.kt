package com.blivtech.emptrack.data.model

import androidx.annotation.DrawableRes

/** A single home-screen ad / announcement slide. */
data class Banner(
    val badge: String,
    val title: String,
    val subtitle: String,
    val cta: String,
    @DrawableRes val bgRes: Int
)