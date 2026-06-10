package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val apiService: ApiService
) {


    suspend fun addOvertime(req: OvertimeRequest): Resource<String> =
        safeCall { apiService.addOvertime(req) }


    suspend fun addAdvance(req: AdvanceRequest): Resource<String> =
        safeCall { apiService.addAdvance(req) }

    // ─── Bonus ──────────────────────

    suspend fun addBonus(req: BonusRequest): Resource<String> =
        safeCall { apiService.addBonus(req) }

    // ─── Safe call helper ───────────
    private suspend fun <T> safeCall(
        call: suspend () -> retrofit2.Response<ApiResponse<T>>
    ): Resource<T> {
        return try {
            val response = call()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Request failed")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }
}