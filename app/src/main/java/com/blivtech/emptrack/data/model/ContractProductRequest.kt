package com.blivtech.emptrack.data.model

// ─────────────────────────────────────
// ✅ Contract product request
// ─────────────────────────────────────
data class ContractProductRequest(
    val btCode: String,
    val companyCode: String,
    val productName: String,
    val workName: String,
    val ratePerUnit: Double,
    val unit: String,
    val colorTag: String = "#1565C0"
)

// ─────────────────────────────────────
// ✅ Contract entry row (for UI)
// ─────────────────────────────────────
data class ContractEntryRow(
    val rowId: String = java.util.UUID.randomUUID().toString(),
    val productId: String = "",
    val productName: String = "",
    val workName: String = "",
    val ratePerUnit: Double = 0.0,
    val unit: String = "",
    val colorTag: String = "#1565C0",
    val quantityDone: Double = 0.0,
    val totalAmount: Double = 0.0
)

// ─────────────────────────────────────
// ✅ Contract entry request (to API)
// ─────────────────────────────────────
data class ContractEntryRequest(
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val shiftName: String,
    val entryDate: String,
    val entries: List<ContractEntryDetail>
)

data class ContractEntryDetail(
    val productId: String,
    val productName: String,
    val workName: String,
    val quantityDone: Double,
    val ratePerUnit: Double,
    val totalAmount: Double,
    val unit: String
)

// ─────────────────────────────────────
// ✅ Summary item (for monthly view)
// ─────────────────────────────────────
data class ContractSummaryItem(
    val productName: String,
    val workName: String,
    val totalQty: Double,
    val ratePerUnit: Double,
    val totalAmount: Double,
    val unit: String
)