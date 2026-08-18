package com.blivtech.emptrack.data.model

/** Response row from GET /work-entries and POST .../bulk. */
data class WorkEntryDto(
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val entryDate: String,
    val employeeId: Long,
    val productId: Long,
    val productName: String,
    val workTypeId: Long,
    val workName: String,
    val unit: String,
    val rate: Double,
    val pieces: Int,
    val amount: Double,
    val status: Int
)

/** One line in the bulk save payload. Rate/name/amount are resolved server-side. */
data class WorkEntryItemDto(
    val productId: Long,
    val workTypeId: Long,
    val pieces: Int
)

/** Body for POST /work-entries/{companyCode}/bulk — the full working set for one worker/date. */
data class BulkSaveWorkEntryRequestDto(
    val entryDate: String,
    val employeeId: Long,
    val items: List<WorkEntryItemDto>
)

/** Per-worker totals for the Daily Entry list. */
data class DayTotalDto(
    val employeeId: Long,
    val pieces: Int,
    val amount: Double
)