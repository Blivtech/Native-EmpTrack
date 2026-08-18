package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.BulkSaveWorkEntryRequestDto
import com.blivtech.emptrack.data.model.DayTotalDto
import com.blivtech.emptrack.data.model.WorkEntry
import com.blivtech.emptrack.data.model.WorkEntryDto
import com.blivtech.emptrack.data.model.WorkEntryItemDto
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

/** API-only. No Room. Every read and write goes to the server. */
@Singleton
class WorkEntryRepository @Inject constructor(
    private val api: ApiService
) {

    suspend fun getEntries(companyCode: String, employeeId: Long, date: String): Resource<List<WorkEntry>> =
        try {
            val res = api.getEntries(companyCode, employeeId, date)
            val body = res.body()
            if (res.isSuccessful && body != null && body.code == 200) {
                Resource.Success(body.data.orEmpty().map { it.toModel() })
            } else {
                Resource.Error(body?.message ?: "Could not load entries")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error while loading entries")
        }

    /** Push the full working set. Server replaces that worker/date's rows and returns the saved list. */
    suspend fun saveEntries(
        btCode: String, companyCode: String, entryDate: String, employeeId: Long,
        entries: List<WorkEntry>
    ): Resource<List<WorkEntry>> = try {
        val request = BulkSaveWorkEntryRequestDto(
            entryDate = entryDate,
            employeeId = employeeId,
            items = entries.map { WorkEntryItemDto(it.productId, it.workTypeId, it.pieces) }
        )
        val res = api.saveEntries(companyCode, btCode, request)
        val body = res.body()
        if (res.isSuccessful && body != null && body.code == 200) {
            Resource.Success(body.data.orEmpty().map { it.toModel() })
        } else {
            Resource.Error(body?.message ?: "Could not save entries")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error while saving entries")
    }

    suspend fun getDayTotals(companyCode: String, date: String): List<DayTotalDto> = try {
        val res = api.getDayTotals(companyCode, date)
        val body = res.body()
        if (res.isSuccessful && body != null && body.code == 200) body.data.orEmpty() else emptyList()
    } catch (e: Exception) {
        emptyList()
    }

    private fun WorkEntryDto.toModel() = WorkEntry(
        clientId = 0L, serverId = id, btCode = btCode, companyCode = companyCode,
        entryDate = entryDate, employeeId = employeeId,
        productId = productId, productName = productName,
        workTypeId = workTypeId, workName = workName, unit = unit,
        rate = rate, pieces = pieces, amount = amount
    )
}