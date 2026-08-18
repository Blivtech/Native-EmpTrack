package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.model.LoginRequest
import com.blivtech.emptrack.data.model.LoginResponse
import com.blivtech.emptrack.data.model.RegisterRequest
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.CommonClass
import com.blivtech.emptrack.utils.Resource
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun register(request: RegisterRequest): Resource<Any> {
        return try {
            val response = apiService.register(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200) Resource.Success(body.data ?: Any())
                else Resource.Error(body.message)
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = CommonClass. parseErrorMessage(errorBody)
                    ?: "Failed to create employee"
                Resource.Error(errorMessage)
            }

        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

    suspend fun login(request: LoginRequest): Resource<LoginResponse> {
        return try {
            val response = apiService.login(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null)
                    Resource.Success(body.data)
                else Resource.Error(body.message)
            } else {
                Resource.Error("Login failed. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

}