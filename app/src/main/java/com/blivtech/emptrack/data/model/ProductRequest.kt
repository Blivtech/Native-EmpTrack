package com.blivtech.emptrack.data.model

// ─── Product ────────────────────────────────
data class ProductRequest(
    val btCode: String,
    val companyCode: String,
    val productName: String,
    val description: String?,
    val workTypes: List<WorkTypeRequest>
)

data class WorkTypeRequest(
    val workTypeName: String,
    val ratePerPiece: Double,
    val unit: String,
    val colorTag: String
)

data class ProductResponse(
    val productId: String,
    val btCode: String,
    val companyCode: String,
    val productName: String,
    val description: String?,
    val status: Int,
    val workTypes: List<WorkTypeResponse>
)

data class WorkTypeResponse(
    val workTypeId: String,
    val productId: String,
    val workTypeName: String,
    val ratePerPiece: Double,
    val unit: String,
    val colorTag: String,
    val status: Int
)

// ─── Work Entry ─────────────────────────────
data class WorkEntryRequest(
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val entryDate: String,
    val entries: List<WorkEntryDetail>
)

data class WorkEntryDetail(
    val productId: String,
    val workTypeId: String,
    val piecesDone: Double,
    val ratePerPiece: Double,
    val totalAmount: Double,
    val remarks: String?
)

data class WorkEntryResponse(
    val entryId: String,
    val btCode: String,
    val companyCode: String,
    val empCode: String,
    val empName: String,
    val productId: String,
    val productName: String,
    val workTypeId: String,
    val workTypeName: String,
    val entryDate: String,
    val piecesDone: Double,
    val ratePerPiece: Double,
    val totalAmount: Double,
    val remarks: String?,
    val status: Int
)

// ─── UI Model ───────────────────────────────
data class WorkEntryRow(
    val id: Int,
    var productId: String = "",
    var productName: String = "",
    var workTypeId: String = "",
    var workTypeName: String = "",
    var ratePerPiece: Double = 0.0,
    var piecesDone: Double = 0.0,
    var totalAmount: Double = 0.0
) {
    val isValid: Boolean
        get() = productId.isNotEmpty() &&
                workTypeId.isNotEmpty() &&
                piecesDone > 0.0
}