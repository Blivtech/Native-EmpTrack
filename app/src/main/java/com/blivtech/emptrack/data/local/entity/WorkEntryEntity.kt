package com.blivtech.emptrack.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One logged line: worker did `pieces` of `workName` on `product` for `entryDate`.
 * Unique (employeeId, entryDate, productId, workTypeId) enforces find-or-update.
 * serverId/syncStatus are for pushing to the backend later.
 */
@Entity(
    tableName = "work_entries",
    indices = [
        Index("companyCode"), Index("employeeId"), Index("entryDate"),
        Index(value = ["employeeId", "entryDate", "productId", "workTypeId"], unique = true)
    ]
)
data class WorkEntryEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Long? = null,
    val btCode: String,
    val companyCode: String,
    val entryDate: String,        // yyyy-MM-dd
    val employeeId: Long,
    val productId: Long,
    val productName: String,
    val workTypeId: Long,
    val workName: String,
    val unit: String,
    val rate: Double,
    val pieces: Int,
    val amount: Double,           // pieces * rate
    val syncStatus: Int = 1,      // 0 = synced, 1 = pending push
    val status: Int = 1
)