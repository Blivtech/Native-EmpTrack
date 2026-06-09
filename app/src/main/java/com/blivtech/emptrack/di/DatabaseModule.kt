package com.blivtech.emptrack.di

import android.content.Context
import androidx.room.Room
import com.blivtech.emptrack.data.local.dao.*
import com.blivtech.emptrack.data.local.db.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "emptrack_db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideCompanyDao(db: AppDatabase): CompanyDao = db.companyDao()
    @Provides fun provideShiftDao(db: AppDatabase): ShiftDao = db.shiftDao()
    @Provides fun provideDepartmentDao(db: AppDatabase): DepartmentDao = db.departmentDao()
    @Provides fun provideEmployeeDao(db: AppDatabase): EmployeeDao = db.employeeDao()
    @Provides
    fun provideDesignationDao(db: AppDatabase): DesignationDao = db.designationDao()
    @Provides
    fun provideShiftPlanDao (db: AppDatabase): ShiftPlanDao = db.shiftPlanDao()
}