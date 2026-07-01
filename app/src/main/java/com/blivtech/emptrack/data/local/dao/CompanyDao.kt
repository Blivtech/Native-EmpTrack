package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompanyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(companies: List<CompanyEntity>)

    @Query("SELECT * FROM tbl_companies WHERE  status = 1")
    fun getCompaniesByBtCode(): Flow<List<CompanyEntity>>

    @Query("SELECT * FROM tbl_companies WHERE id = :id")
    suspend fun getCompanyById(id: Long): CompanyEntity?

    @Query("DELETE FROM tbl_companies WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)
    @Query("DELETE FROM tbl_companies WHERE id = :id")
    suspend fun deleteById(id: Long)
}