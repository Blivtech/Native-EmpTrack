package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface EmployeeDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(employee: EmployeeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(employee: List<EmployeeEntity>)

    @Update
    suspend fun update(employee: EmployeeEntity)

    @Query("SELECT * FROM tbl_employees WHERE companyCode = :companyCode AND status = 1")
    fun getEmployeesByCompany(companyCode: String): Flow<List<EmployeeEntity>>

    @Query("SELECT * FROM tbl_employees WHERE btCode = :btCode AND status = 1")
    fun getEmployeesByBtCode(btCode: String): Flow<List<EmployeeEntity>>

    @Query("SELECT COUNT(*) FROM tbl_employees WHERE companyCode = :companyCode AND status = 1")
    fun getEmployeeCount(companyCode: String): Flow<Int>

    @Query("SELECT * FROM tbl_employees WHERE empCode = :empCode and companyCode =:companyCode")
    suspend fun getEmployeeById(empCode: String, companyCode: String): EmployeeEntity?

    @Query("DELETE FROM tbl_employees WHERE empCode = :empCode and companyCode =:companyCode")
    suspend fun deleteById(empCode: String, companyCode: String)


}