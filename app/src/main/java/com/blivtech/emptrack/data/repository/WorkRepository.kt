package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkRepository @Inject constructor(
    private val apiService: ApiService
) {

    // ✅ Get products
    suspend fun getProducts(
        btCode: String, companyCode: String
    ): Resource<List<ProductResponse>> {
        return try {
            val response = apiService.getProducts(btCode, companyCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to load products")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Add product
    suspend fun addProduct(request: ProductRequest): Resource<String> {
        return try {
            val response = apiService.addProduct(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200)
                    Resource.Success("Product saved successfully")
                else Resource.Error(body.message)
            } else Resource.Error("Failed to save product")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Update product
    suspend fun updateProduct(
        productId: String, request: ProductRequest
    ): Resource<String> {
        return try {
            val response = apiService.updateProduct(productId, request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200)
                    Resource.Success("Product updated successfully")
                else Resource.Error(body.message)
            } else Resource.Error("Failed to update product")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Delete product
    suspend fun deleteProduct(productId: String): Resource<String> {
        return try {
            val response = apiService.deleteProduct(productId)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200)
                    Resource.Success("Deleted successfully")
                else Resource.Error(body.message)
            } else Resource.Error("Failed to delete")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Add work entry
    suspend fun addWorkEntry(request: WorkEntryRequest): Resource<String> {
        return try {
            val response = apiService.addWorkEntry(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200)
                    Resource.Success("Work entry saved successfully")
                else Resource.Error(body.message)
            } else Resource.Error("Failed to save work entry")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Get work entries
    suspend fun getWorkEntries(
        btCode: String,
        companyCode: String,
        date: String? = null,
        month: String? = null
    ): Resource<List<WorkEntryResponse>> {
        return try {
            val response = apiService.getWorkEntries(btCode, companyCode, date, month)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else Resource.Error("Failed to load entries")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }
}