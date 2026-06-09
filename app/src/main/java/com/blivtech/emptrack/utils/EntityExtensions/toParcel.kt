package com.blivtech.emptrack.utils.EntityExtensions

import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.parcel.CompanyParcel
import com.blivtech.emptrack.data.model.parcel.EmployeeParcel
import com.blivtech.emptrack.data.model.parcel.ShiftParcel

// ✅ CompanyEntity → CompanyParcel
fun CompanyEntity.toParcel() = CompanyParcel(
    id = id,
    btCode = btCode,
    companyCode = companyCode,
    name = name,
    address = address,
    city = city,
    state = state,
    phone = phone,
    email = email,
    logo = logo,
    status = status
)

// ✅ CompanyParcel → CompanyEntity
fun CompanyParcel.toEntity() = CompanyEntity(
    id = id,
    btCode = btCode,
    companyCode = companyCode,
    name = name,
    address = address,
    city = city,
    state = state,
    phone = phone,
    email = email,
    logo = logo,
    status = status
)

// ✅ ShiftEntity → ShiftParcel
fun ShiftEntity.toParcel() = ShiftParcel(
    id = id,
    btCode = btCode,
    companyCode = companyCode,
    shiftCode = shiftCode,
    shiftName = shiftName,
    startTime = startTime,
    endTime = endTime,
    status = status
)

// ✅ ShiftParcel → ShiftEntity
fun ShiftParcel.toEntity() = ShiftEntity(
    id = id,
    btCode = btCode,
    companyCode = companyCode,
    shiftCode = shiftCode,
    shiftName = shiftName,
    startTime = startTime,
    endTime = endTime,
    status = status
)

// ✅ EmployeeEntity → EmployeeParcel
fun EmployeeEntity.toParcel() = EmployeeParcel(
    id                = id,
    btCode            = btCode,
    empCode           = empCode,
    desgCode         = desgCode,
    deptCode       = deptCode,
    companyCode      = companyCode,
    name              = name,
    email             = email,
    phone             = phone,
    gender            = gender,
    dob               = dob,
    joiningDate       = joiningDate,
    profileImage      = profileImage,
    salaryType        = salaryType,
    salaryAmount      = salaryAmount,
    lastAppraisalDate = lastAppraisalDate,
    status            = status
)

// ✅ EmployeeParcel → EmployeeEntity
fun EmployeeParcel.toEntity() = EmployeeEntity(
    id                = id,
    btCode            = btCode,
    empCode           = empCode,
    companyCode         = companyCode,
    desgCode      = desgCode,
    deptCode     = deptCode,
    name              = name,
    email             = email,
    phone             = phone,
    gender            = gender,
    dob               = dob,
    joiningDate       = joiningDate,
    profileImage      = profileImage,
    salaryType        = salaryType,
    salaryAmount      = salaryAmount,
    lastAppraisalDate = lastAppraisalDate,
    status            = status
)