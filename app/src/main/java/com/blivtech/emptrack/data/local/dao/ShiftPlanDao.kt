package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.ShiftPlanEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShiftPlanDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plan: ShiftPlanEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plans: List<ShiftPlanEntity>)

    // ✅ Get all plans for a week
    @Query("""
        SELECT * FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND weekStartDate = :weekStartDate
        AND status = 1
    """)
    fun getWeekPlan(
        companyCode: String,
        weekStartDate: String
    ): Flow<List<ShiftPlanEntity>>

    // ✅ Get employees for a specific shift + week
    @Query("""
        SELECT * FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND shiftCode = :shiftCode 
        AND weekStartDate = :weekStartDate
        AND status = 1
    """)
    suspend fun getShiftEmployees(
        companyCode: String,
        shiftCode: String,
        weekStartDate: String
    ): List<ShiftPlanEntity>

    // ✅ Get last week plan (for copy)
    @Query("""
        SELECT * FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND weekStartDate = :lastWeekStartDate
        AND status = 1
    """)
    suspend fun getLastWeekPlan(
        companyCode: String,
        lastWeekStartDate: String
    ): List<ShiftPlanEntity>

    // ✅ Delete all plans for a week + shift (before re-saving)
    @Query("""
        DELETE FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND shiftCode = :shiftCode 
        AND weekStartDate = :weekStartDate
    """)
    suspend fun deleteShiftWeekPlan(
        companyCode: String,
        shiftCode: String,
        weekStartDate: String
    )

    // ✅ Delete all plans for a week (for copy from last week)
    @Query("""
        DELETE FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND weekStartDate = :weekStartDate
    """)
    suspend fun deleteWeekPlan(
        companyCode: String,
        weekStartDate: String
    )

    // ✅ Check if week plan exists
    @Query("""
        SELECT COUNT(*) FROM tbl_shift_plan 
        WHERE companyCode = :companyCode 
        AND weekStartDate = :weekStartDate
    """)
    suspend fun getWeekPlanCount(
        companyCode: String,
        weekStartDate: String
    ): Int

    // ✅ In ShiftPlanDao.kt — return full entity
    @Query("""
    SELECT * FROM tbl_shift_plan 
    WHERE companyCode = :companyCode 
    AND shiftCode = :shiftCode
    AND status = 1
""")
    suspend fun getAssignedEmpIds(
        companyCode: String,
        shiftCode: String,
    ): List<ShiftPlanEntity>

    // ✅ Get shift count for company
    @Query("""
    SELECT COUNT(DISTINCT shiftCode) 
    FROM tbl_shift_plan 
    WHERE companyCode = :companyCode
    AND status = 1
""")
    suspend fun getShiftCount(companyCode: String): Int
}