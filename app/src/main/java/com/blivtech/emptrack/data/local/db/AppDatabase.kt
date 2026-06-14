package com.blivtech.emptrack.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.blivtech.emptrack.data.local.dao.*
import com.blivtech.emptrack.data.local.entity.*

@Database(
    entities = [
        CompanyEntity::class,
        ShiftEntity::class,
        DepartmentEntity::class,
        DesignationEntity::class,
        ShiftPlanEntity::class,
        EmployeeEntity::class,
        ContractProductEntity::class,
        UnitEntity::class,
        ContractEntryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun companyDao(): CompanyDao
    abstract fun shiftDao(): ShiftDao
    abstract fun departmentDao(): DepartmentDao
    abstract fun designationDao(): DesignationDao
    abstract fun employeeDao(): EmployeeDao
    abstract fun shiftPlanDao(): ShiftPlanDao
    abstract fun contractProductDao(): ContractProductDao
    abstract fun unitDao(): UnitDao
    abstract fun contractEntryDao(): ContractEntryDao
}