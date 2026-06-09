package com.blivtech.emptrack.data.model

data class MasterResponse(
    val code: Int,
    val message: String,
    val data: MasterData?
)

data class MasterData(
    val companies: List<CompanyData>,
    val designations: List<DesignationData>,
    val departments: List<DepartmentData>,
    val employees: List<EmployeeData>
)

data class CompanyData(
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val name: String,
    val address: String?,
    val city: String?,
    val state: String?,
    val phone: String?,
    val email: String?,
    val logo: String?,
    val status: Int,
    val shifts: List<ShiftData>
)

data class ShiftData(
    val id: Long,
    val btCode: String,
    val companyCode: String,
    val shiftCode: String,
    val shiftName: String,
    val startTime: String,
    val endTime: String,
    val status: Int
)

data class DepartmentData(
    val id: Long,
    val btCode: String,
    val deptCode: String,
    val name: String,
    val description: String?,
    val status: Int
)

data class DesignationData(
    val id: Long,
    val btCode: String,
    val desgCode: String,
    val name: String,
    val description: String?,
    val status: Int
)
data class EmployeeData(
    val id: Long,
    val btCode: String,
    val empCode: String,
    val companyCode: String,
    val deptCode: String,
    val desgCode: String,
    val name: String,
    val email: String?,
    val phone: String?,
    val gender: Int?,
    val dob: String?,
    val joiningDate: String?,
    val profileImage: String?,
    val salaryType: Int?,
    val salaryAmount: Double?,
    val lastAppraisalDate: String?,
    val status: Int
)