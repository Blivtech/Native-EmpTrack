package com.blivtech.emptrack.ui.attendance

import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ShiftStatusResponse

data class ShiftCardItem(
    val shift: ShiftEntity,
    val index: Int,
    val status: ShiftStatusResponse? = null
)