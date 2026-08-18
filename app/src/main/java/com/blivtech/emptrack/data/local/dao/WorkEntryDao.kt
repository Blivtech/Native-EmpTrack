package com.blivtech.emptrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.blivtech.emptrack.data.local.entity.WorkEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkEntryDao {

    @Query("""
        SELECT * FROM work_entries
        WHERE companyCode = :companyCode AND employeeId = :employeeId
          AND entryDate = :date AND status = 1
        ORDER BY productName, workName
    """)
    fun observeByEmployeeAndDate(companyCode: String, employeeId: Long, date: String): Flow<List<WorkEntryEntity>>

    /** Per-worker totals for a date (worker list screen). */
    @Query("""
        SELECT employeeId AS employeeId, SUM(pieces) AS pieces, SUM(amount) AS amount
        FROM work_entries
        WHERE companyCode = :companyCode AND entryDate = :date AND status = 1
        GROUP BY employeeId
    """)
    fun observeDayTotals(companyCode: String, date: String): Flow<List<EmployeeDayTotal>>

    @Query("""
        SELECT * FROM work_entries
        WHERE companyCode = :companyCode AND employeeId = :employeeId AND entryDate = :date
          AND productId = :productId AND workTypeId = :workTypeId AND status = 1
        LIMIT 1
    """)
    suspend fun find(companyCode: String, employeeId: Long, date: String, productId: Long, workTypeId: Long): WorkEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: WorkEntryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<WorkEntryEntity>)

    @Update
    suspend fun update(entry: WorkEntryEntity)

    @Query("DELETE FROM work_entries WHERE localId = :localId")
    suspend fun deleteById(localId: Long)

    @Query("DELETE FROM work_entries WHERE companyCode = :companyCode AND employeeId = :employeeId AND entryDate = :date")
    suspend fun deleteByEmployeeDate(companyCode: String, employeeId: Long, date: String)
}

/** Projection for observeDayTotals. */
data class EmployeeDayTotal(
    val employeeId: Long,
    val pieces: Int,
    val amount: Double
)