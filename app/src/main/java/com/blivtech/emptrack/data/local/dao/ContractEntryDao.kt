package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.ContractEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractEntryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: ContractEntryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(entries: List<ContractEntryEntity>)

    @Update
    suspend fun update(entry: ContractEntryEntity)

    // ✅ Get by month
    @Query("""
        SELECT * FROM tbl_contract_entries
        WHERE btCode = :btCode AND companyCode = :companyCode
        AND substr(entryDate, 1, 7) = :month
        ORDER BY entryDate DESC, createdAt DESC
    """)
    fun getEntriesByMonth(
        btCode: String,
        companyCode: String,
        month: String
    ): Flow<List<ContractEntryEntity>>

    // ✅ Get by date + shift
    @Query("""
        SELECT * FROM tbl_contract_entries
        WHERE btCode = :btCode AND companyCode = :companyCode
        AND entryDate = :date AND shiftCode = :shiftCode
        ORDER BY createdAt ASC
    """)
    suspend fun getEntriesByDateShift(
        btCode: String,
        companyCode: String,
        date: String,
        shiftCode: String
    ): List<ContractEntryEntity>

    @Query("SELECT * FROM tbl_contract_entries WHERE entryId = :entryId")
    suspend fun getEntryById(entryId: String): ContractEntryEntity?

    @Query("DELETE FROM tbl_contract_entries WHERE entryId = :entryId")
    suspend fun deleteEntry(entryId: String)

    @Query("DELETE FROM tbl_contract_entries WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)
}