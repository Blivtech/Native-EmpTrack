package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.*
import com.blivtech.emptrack.data.local.entity.*
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.ui.home.SyncResult
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MasterRepository @Inject constructor(
    private val apiService: ApiService,
    private val companyDao: CompanyDao,
    private val shiftDao: ShiftDao,
    private val departmentDao: DepartmentDao,
    private val employeeDao: EmployeeDao,
    private val designationDao: DesignationDao,
) {
    suspend fun syncMasterData(btCode: String): Resource<SyncResult> {
        return try {
            val response = apiService.getMasterData(btCode)
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.code == 200 && body.data != null) {
                    val data = body.data

                    // Clear old data
                    companyDao.deleteByBtCode(btCode)
                    shiftDao.deleteByBtCode(btCode)
                    departmentDao.deleteByBtCode(btCode)
                    designationDao.deleteByBtCode(btCode)

                    // Save companies
                    val companies = data.companies.map { c ->
                        CompanyEntity(
                            id = c.id,
                            btCode = c.btCode,
                            companyCode = c.companyCode,
                            name = c.name,
                            address = c.address,
                            city = c.city,
                            state = c.state,
                            phone = c.phone,
                            email = c.email,
                            logo = c.logo,
                            status = c.status
                        )
                    }
                    companyDao.insertAll(companies)

                    // Save shifts
                    val shifts = data.companies.flatMap { c ->
                        c.shifts.map { s ->
                            ShiftEntity(
                                id = s.id,
                                btCode = s.btCode,
                                companyCode = s.companyCode,
                                shiftCode = s.shiftCode,
                                shiftName = s.shiftName,
                                startTime = s.startTime,
                                endTime = s.endTime,
                                status = s.status
                            )
                        }
                    }
                    shiftDao.insertAll(shifts)

                    // Save departments
                    val departments = data.departments.map { d ->
                        DepartmentEntity(
                            id = d.id,
                            btCode = d.btCode,
                            deptCode = d.deptCode,
                            name = d.name,
                            description = d.description,
                            status = d.status
                        )
                    }
                    departmentDao.insertAll(departments)


                    val designations = data.designations.map { d ->
                        DesignationEntity(
                            id = d.id,
                            btCode = d.btCode,
                            desgCode = d.desgCode,
                            name = d.name,
                            description = d.description,
                            status = d.status
                        )
                    }
                    designationDao.insertAll(designations)

                    val employeeEntities = data.employees.map { d ->
                        EmployeeEntity(
                            id                = d.id,
                            btCode            = d.btCode,
                            empCode           = d.empCode,
                            companyCode       = d.companyCode,
                            deptCode          = d.deptCode,
                            desgCode          = d.desgCode,
                            name              = d.name,
                            email             = d.email,
                            phone             = d.phone,
                            gender            = d.gender,
                            dob               = d.dob,
                            joiningDate       = d.joiningDate,
                            profileImage      = d.profileImage,
                            salaryType        = d.salaryType,
                            salaryAmount      = d.salaryAmount,
                            lastAppraisalDate = d.lastAppraisalDate,
                            status            = d.status
                        )
                    }
                    employeeDao.insertAll(employeeEntities)



                    Resource.Success(
                        SyncResult(
                            companiesCount = companies.size,
                            shiftsCount = shifts.size,
                            departmentsCount = departments.size
                        )
                    )
                } else {
                    Resource.Error(body.message)
                }
            } else {
                Resource.Error("Failed to sync. Please try again.")
            }
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Network error occurred")
        }
    }

    fun getCompanies(): Flow<List<CompanyEntity>> =
        companyDao.getCompaniesByBtCode()

    fun getShiftsByCompany(companyId: String): Flow<List<ShiftEntity>> =
        shiftDao.getShiftsByCompany(companyId)

    fun getDepartments(btCode: String): Flow<List<DepartmentEntity>> =
        departmentDao.getDepartmentsByBtCode(btCode)
}