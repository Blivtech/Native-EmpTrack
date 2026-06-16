package com.blivtech.emptrack.data.model

import com.blivtech.emptrack.data.local.entity.ShiftEntity

data class ShiftPlanCardItem(
    val shift: ShiftEntity,
    val index: Int,
    val empCount: Int,
    val empNames: List<String>
)