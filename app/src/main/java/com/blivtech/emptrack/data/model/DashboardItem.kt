package com.blivtech.emptrack.data.model

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes

data class DashboardItem(
    val title: String,
    @DrawableRes val icon: Int,
    @ColorRes val iconColor: Int,
    @ColorRes val iconBgColor: Int
)