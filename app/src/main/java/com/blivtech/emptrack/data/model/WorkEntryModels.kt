package com.blivtech.emptrack.data.model

/**
 * NEW MODELS for the redesigned Work-Entry flow.
 *
 * Flow:  Select Date  ->  Employee list (per date, with indicator)  ->  Add / Edit / Delete entry
 *
 * NOTE: WorkEntryRow, WorkEntryRequest, WorkEntryDetail, ProductResponse, WorkTypeResponse
 * already exist in your project — they are NOT redefined here.
 */

// ─────────────────────────────────────────────
// ① Per-date summary coming back from the API.
//    Used to decide which employees already have an entry
//    on the selected date (the "indicator").
//    Endpoint (assumed):
//    GET /api/v1/work-entries/summary?btCode=&companyCode=&entryDate=
// ─────────────────────────────────────────────
data class DateEntrySummary(
    val empCode: String,
    val entryId: String,          // server id of that day's entry — needed for edit/delete
    val totalAmount: Double = 0.0,
    val entryCount: Int = 0
)

// ─────────────────────────────────────────────
// ② The merged row shown in the employee list screen.
//    = local employee (roster)  +  API summary for the date.
// ─────────────────────────────────────────────
data class EmployeeEntryStatus(
    val empCode: String,
    val empName: String,
    val hasEntry: Boolean = false,
    val entryId: String? = null,
    val totalAmount: Double = 0.0,
    val entryCount: Int = 0
)


data class WorkEntryResponse(
    val entryId: String,
    val empCode: String,
    val empName: String,
    val entryDate: String,
    val details: List<WorkEntryDetailResponse> = emptyList()
)

data class WorkEntryDetailResponse(
    val productId: String,
    val productName: String,
    val workTypeId: String,
    val workTypeName: String,
    val piecesDone: Double,
    val ratePerPiece: Double,
    val totalAmount: Double,
    val colorTag: String? = null
)
