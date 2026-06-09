package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DepartmentDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(departments: List<DepartmentEntity>)

    @Query("SELECT * FROM tbl_departments WHERE btCode = :btCode AND status = 1")
    fun getDepartmentsByBtCode(btCode: String): Flow<List<DepartmentEntity>>

    @Query("DELETE FROM tbl_departments WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)
}