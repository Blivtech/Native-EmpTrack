package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.ProductDao
import com.blivtech.emptrack.data.local.entity.ProductEntity
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.local.entity.ProductWorkEntity
import com.blivtech.emptrack.data.model.ProductDto
import com.blivtech.emptrack.data.model.ProductRequestDto
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productApi: ApiService,
    private val productDao: ProductDao
) {

    /** Live product list for a company, straight from Room (offline-friendly). */
    fun observeProducts(companyCode: String): Flow<List<ProductWithWorks>> =
        productDao.observeByCompany(companyCode)

    suspend fun createProduct(
        btCode: String,
        companyCode: String,
        body: ProductRequestDto
    ): Resource<Unit> = try {
        val response = productApi.createProduct(btCode, companyCode, body)
        val apiBody = response.body()
        if (response.isSuccessful && apiBody != null && apiBody.code == 200 && apiBody.data != null) {
            saveOne(apiBody.data, btCode, companyCode)
            Resource.Success(Unit)
        } else {
            Resource.Error(apiBody?.message ?: "Failed to create product")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error occurred")
    }

    suspend fun updateProduct(
        btCode: String,
        companyCode: String,
        id: Long,
        body: ProductRequestDto
    ): Resource<Unit> = try {
        val response = productApi.updateProduct(id, btCode, companyCode, body)
        val apiBody = response.body()
        if (response.isSuccessful && apiBody != null && apiBody.code == 200 && apiBody.data != null) {
            saveOne(apiBody.data, btCode, companyCode)
            Resource.Success(Unit)
        } else {
            Resource.Error(apiBody?.message ?: "Failed to update product")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error occurred")
    }

    suspend fun deleteProduct(
        btCode: String,
        companyCode: String,
        id: Long
    ): Resource<Unit> = try {
        val response = productApi.deleteProduct(id, btCode, companyCode)
        val apiBody = response.body()
        if (response.isSuccessful && apiBody != null && apiBody.code == 200) {
            productDao.deleteWorksByProduct(id)
            productDao.deleteById(id)
            Resource.Success(Unit)
        } else {
            Resource.Error(apiBody?.message ?: "Failed to delete product")
        }
    } catch (e: Exception) {
        Resource.Error(e.localizedMessage ?: "Network error occurred")
    }

    /** Persist one product + its works locally (scope from params, since the server may omit it). */
    private suspend fun saveOne(dto: ProductDto, btCode: String, companyCode: String) {
        productDao.deleteWorksByProduct(dto.id)
        productDao.insertAll(
            listOf(
                ProductEntity(
                    id = dto.id,
                    btCode = btCode,
                    companyCode = companyCode,
                    productCode = dto.productCode,
                    name = dto.name,
                    unit = dto.unit,
                    icon = dto.icon,
                    status = dto.status
                )
            )
        )
        productDao.insertAllWorks(
            dto.works.map { w ->
                ProductWorkEntity(
                    productId = dto.id,
                    btCode = btCode,
                    companyCode = companyCode,
                    workTypeId = w.workTypeId,
                    workCode = w.workCode,
                    workName = w.workName,
                    rate = w.rate
                )
            }
        )
    }
}