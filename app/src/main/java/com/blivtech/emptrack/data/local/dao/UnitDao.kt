package com.blivtech.emptrack.data.local.dao

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.UnitEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UnitDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(unit: UnitEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(units: List<UnitEntity>)

    @Query("SELECT * FROM tbl_units WHERE btCode = :btCode ORDER BY id ASC")
    fun getUnits(btCode: String): Flow<List<UnitEntity>>

    @Query("SELECT * FROM tbl_units WHERE btCode = :btCode ORDER BY id ASC")
    suspend fun getUnitsSync(btCode: String): List<UnitEntity>

    @Query("SELECT COUNT(*) FROM tbl_units WHERE btCode = :btCode")
    suspend fun getUnitCount(btCode: String): Int

    @Query("DELETE FROM tbl_units WHERE id = :id")
    suspend fun deleteById(id: Int)
}