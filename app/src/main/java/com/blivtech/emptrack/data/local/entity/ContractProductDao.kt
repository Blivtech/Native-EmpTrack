package com.blivtech.emptrack.data.local.entity

import androidx.room.*
import com.blivtech.emptrack.data.local.entity.ContractProductEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContractProductDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(product: ContractProductEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(products: List<ContractProductEntity>)

    @Update
    suspend fun update(product: ContractProductEntity)

    @Query("SELECT * FROM tbl_contract_products WHERE btCode = :btCode AND companyCode = :companyCode AND status = 1")
    fun getProducts(btCode: String, companyCode: String): Flow<List<ContractProductEntity>>

    @Query("SELECT * FROM tbl_contract_products WHERE productId = :productId")
    suspend fun getProductById(productId: String): ContractProductEntity?

    @Query("UPDATE tbl_contract_products SET status = 2 WHERE productId = :productId")
    suspend fun deleteProduct(productId: String)

    @Query("DELETE FROM tbl_contract_products WHERE btCode = :btCode")
    suspend fun deleteByBtCode(btCode: String)
}