package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.DepartmentDao
import com.blivtech.emptrack.data.local.dao.DesignationDao
import com.blivtech.emptrack.data.local.dao.EmployeeDao
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.data.model.EmployeeResponse
import com.blivtech.emptrack.data.network.ApiService
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
    // ✅ Get employees from Room
    fun getEmployees(companyId: String): Flow<List<EmployeeEntity>> =
        employeeDao.getEmployeesByCompany(companyId)

    // ✅ Get employee by id
    suspend fun getEmployeeById(id: Long): EmployeeEntity? =
        employeeDao.getEmployeeById(id)

    // ✅ Get departments from Room
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
            } else Resource.Error("Failed to create employee")
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error")
        }
    }

    // ✅ Update employee
    suspend fun updateEmployee(id: Long, request: EmployeeRequest): Resource<EmployeeEntity> {
        return try {
            val response = apiService.updateEmployee(id, request)
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
    suspend fun deleteEmployee(id: Long): Resource<Any> {
        return try {
            val response = apiService.deleteEmployee(id)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200) {
                    employeeDao.deleteById(id)
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
    suspend fun createDepartment(btCode: String, name: String, desc: String): DepartmentEntity {
        val entity = DepartmentEntity(
            id          = System.currentTimeMillis(),
            btCode      = btCode,
            deptCode    = "DEP${System.currentTimeMillis()}",
            name        = name,
            description = desc,
            status      = 1
        )
        departmentDao.insertAll(listOf(entity))
        return entity
    }

    // ✅ Create designation on the fly
    suspend fun createDesignation(btCode: String, name: String, desc: String): DesignationEntity {
        val entity = DesignationEntity(
            id          = System.currentTimeMillis(),
            btCode      = btCode,
            desgCode    = "DESG${System.currentTimeMillis()}",
            name        = name,
            description = desc,
            status      = 1
        )
        designationDao.insertAll(listOf(entity))
        return entity
    }
}