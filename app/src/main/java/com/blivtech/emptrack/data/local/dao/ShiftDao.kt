package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(shifts: List<ShiftEntity>)

    @Query("SELECT * FROM tbl_shifts WHERE companyCode = :companyCode AND status = 1")
    fun getShiftsByCompany(companyCode: String): Flow<List<ShiftEntity>>

    @Query("SELECT * FROM tbl_shifts WHERE btCode = :btCode AND status = 1")
    fun getShiftsByBtCode(btCode: String): Flow<List<ShiftEntity>>

    @Query("DELETE FROM tbl_shifts WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)

    @Query("DELETE FROM tbl_shifts WHERE companyCode = :companyCode")
    suspend fun deleteByCompanyId(companyCode: String)
}