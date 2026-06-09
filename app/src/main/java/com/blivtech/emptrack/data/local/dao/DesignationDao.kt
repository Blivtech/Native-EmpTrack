package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DesignationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(designations: List<DesignationEntity>)

    @Query("SELECT * FROM tbl_designations WHERE btCode = :btCode AND status = 1")
    fun getDesignationsByBtCode(btCode: String): Flow<List<DesignationEntity>>

    @Query("DELETE FROM tbl_designations WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)
}