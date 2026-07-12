package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.DepartmentDao
import com.blivtech.emptrack.data.local.dao.DesignationDao
import com.blivtech.emptrack.data.local.dao.EmployeeDao
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.DepartmentRequest
import com.blivtech.emptrack.data.model.DepartmentResponse
import com.blivtech.emptrack.data.model.DesignationRequest
import com.blivtech.emptrack.data.model.DesignationResponse
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.data.model.EmployeeResponse
import com.blivtech.emptrack.data.model.EmployeeWithDetails
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.CommonClass
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EmployeeRepository @Inject constructor(
    private val apiService: ApiService,
    private val employeeDao: EmployeeDao,
    private val departmentDao: DepartmentDao,
    private val designationDao: DesignationDao
) {
    fun getEmployees(companyId: String): Flow<List<EmployeeEntity>> =
        employeeDao.getEmployeesByCompany(companyId)

    fun getEmployeess(companyId: String): Flow<List<EmployeeWithDetails>> =
        employeeDao.getEmployeesByCompanys(companyId)
    suspend fun getEmployeeById(empCode: String, companyCode: String): EmployeeEntity? =
        employeeDao.getEmployeeById(empCode,companyCode)

    fun getDepartments(btCode: String) =
        departmentDao.getDepartmentsByBtCode(btCode)

    // ✅ Get designations from Room
    fun getDesignations(btCode: String) =
        designationDao.getDesignationsByBtCode(btCode)

    // ✅ Create employee
    suspend fun createEmployee(request: EmployeeRequest): Resource<EmployeeEntity> {
        return try {
            val response = apiService.createEmployee(request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val entity = body.data.toEntity()
                    employeeDao.insert(entity)
                    Resource.Success(entity)
                } else Resource.Error(body.message)
            } else{ val errorBody = response.errorBody()?.string()
                  val errorMessage =CommonClass. parseErrorMessage(errorBody)
                    ?: "Failed to create employee"
                Resource.Error(errorMessage)}
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Update employee
    suspend fun updateEmployee(empCode: String, request: EmployeeRequest): Resource<EmployeeEntity> {
        return try {
            val response = apiService.updateEmployee(empCode, request.companyCode,request)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val entity = body.data.toEntity()
                    employeeDao.update(entity)
                    Resource.Success(entity)
                } else Resource.Error(body.message)
            } else Resource.Error("Failed to update employee")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Delete employee
    suspend fun deleteEmployee(empCode: String, companyCode: String): Resource<Any> {
        return try {
            val response = apiService.deleteEmployee(empCode,companyCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200) {
                    employeeDao.deleteById(empCode,companyCode)
                    Resource.Success(Any())
                } else Resource.Error(body.message)
            } else Resource.Error("Failed to delete employee")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Mapper
    private fun EmployeeResponse.toEntity() = EmployeeEntity(
        id                = id,
        btCode            = btCode,
        empCode           = empCode,
        companyCode         = companyCode,
        deptCode      = deptCode,
        desgCode     = desgCode,
        name              = name,
        email             = email,
        phone             = phone,
        gender            = gender,
        dob               = dob,
        joiningDate       = joiningDate,
        profileImage      = null,           // ✅ Add this
        salaryType        = salaryType,
        salaryAmount      = salaryAmount,
        lastAppraisalDate = null,           // ✅ Add this
        status            = status
    )



    // ✅ Create department on the fly
    suspend fun createDepartment(btCode: String, name: String, desc: String): Resource<DepartmentEntity> {
        return try {
            val response = apiService.saveDepartment(
                DepartmentRequest(
                    btCode      = btCode,
                    deptCode    = "DEP${System.currentTimeMillis()}",
                    name        = name,
                    description = desc,
                    createdAt ="2026-07-06T15:30:00",
                    updatedAt = "2026-07-06T15:30:00",
                    status      = 1
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val entity = body.data.toDepartmentEntity()
                    departmentDao.insertAll(listOf(entity))
                    Resource.Success(entity)
                } else Resource.Error(body.message)
            } else {
                val errorMessage = CommonClass.parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to create department"
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Create designation on the fly
    suspend fun createDesignation(btCode: String, name: String, desc: String): Resource<DesignationEntity> {
        return try {
            val response = apiService.saveDesignation(
                DesignationRequest(
                    btCode      = btCode,
                    desgCode    = "DESG${System.currentTimeMillis()}",
                    name        = name,
                    description = desc,
                    createdAt   ="2026-07-06T15:30:00",
                    updatedAt   = "2026-07-06T15:30:00",
                    status      = 1
                )
            )
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val entity = body.data.toDesignationEntity()
                    designationDao.insertAll(listOf(entity))
                    Resource.Success(entity)
                } else Resource.Error(body.message)
            } else {
                val errorMessage = CommonClass.parseErrorMessage(response.errorBody()?.string())
                    ?: "Failed to create designation"
                Resource.Error(errorMessage)
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }
    private fun DepartmentResponse.toDepartmentEntity() = DepartmentEntity(
        id          = id,
        btCode      = btCode,
        deptCode    = deptCode,
        name        = name,
        description = description,
        status      = status
    )

    private fun DesignationResponse.toDesignationEntity() = DesignationEntity(
        id          = id,
        btCode      = btCode,
        desgCode    = desgCode,
        name        = name,
        description = description,
        status      = status
    )
}