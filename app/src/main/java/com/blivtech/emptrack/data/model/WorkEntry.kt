package com.blivtech.emptrack.data.model

/**
 * Plain in-memory model for a work entry. No Room, no persistence.
 * The screen loads these from the API, edits them locally, and pushes the whole set on Done.
 *
 * clientId  -> stable key for the list/DiffUtil while editing (assigned by the ViewModel)
 * serverId  -> the row id returned by the API (null for rows added this session)
 */
data class WorkEntry(
    val clientId: Long,
    val serverId: Long?,
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
    val amount: Double
)