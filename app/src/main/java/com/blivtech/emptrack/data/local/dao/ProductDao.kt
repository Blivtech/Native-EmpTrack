package com.blivtech.emptrack.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.blivtech.emptrack.data.local.entity.ProductEntity
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.local.entity.ProductWorkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    // ---- sync (same pattern as your other DAOs) ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ProductEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllWorks(works: List<ProductWorkEntity>)

    @Query("DELETE FROM products WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)

    @Query("DELETE FROM product_works WHERE btCode = :btCode")
    suspend fun deleteWorksByBtCode(btCode: String)

    // ---- single-product CRUD (create/edit/delete) ----
    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM product_works WHERE productId = :productId")
    suspend fun deleteWorksByProduct(productId: Long)

    // ---- reads ----
    @Transaction
    @Query("SELECT * FROM products WHERE companyCode = :companyCode AND status = 1 ORDER BY name")
    fun observeByCompany(companyCode: String): Flow<List<ProductWithWorks>>
}