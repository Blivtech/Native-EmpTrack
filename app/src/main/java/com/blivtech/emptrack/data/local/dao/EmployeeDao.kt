package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.EmployeeWithDetails
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
    @Query("""
    SELECT
        e.id,
        e.btCode,
        e.empCode,
        e.companyCode,
        e.deptCode,
        d.name AS deptName,
        e.desgCode,
        des.name AS desgName,
        e.name,
        e.email,
        e.phone,
        e.gender,
        e.dob,
        e.joiningDate,
        e.profileImage,
        e.salaryType,
        e.salaryAmount,
        e.lastAppraisalDate,
        e.status
    FROM tbl_employees e
    LEFT JOIN tbl_departments d
        ON e.deptCode = d.deptCode
    LEFT JOIN tbl_designations des
        ON e.desgCode = des.desgCode
    WHERE e.companyCode = :companyCode
      AND e.status = 1
""")
    fun getEmployeesByCompanys(
        companyCode: String
    ): Flow<List<EmployeeWithDetails>>

}