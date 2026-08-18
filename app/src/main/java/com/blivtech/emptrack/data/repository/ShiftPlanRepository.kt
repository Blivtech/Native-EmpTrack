package com.blivtech.emptrack.data.repository

import com.blivtech.emptrack.data.local.dao.ShiftPlanDao
import com.blivtech.emptrack.data.local.entity.ShiftPlanEntity
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShiftPlanRepository @Inject constructor(
    private val shiftPlanDao: ShiftPlanDao
) {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ✅ Get Monday of a given calendar
    fun getWeekStartDate(calendar: Calendar = Calendar.getInstance()): String {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFmt.format(cal.time)
    }

    // ✅ Get Sunday of a given calendar
    fun getWeekEndDate(calendar: Calendar = Calendar.getInstance()): String {
        val cal = calendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return dateFmt.format(cal.time)
    }

    // ✅ Get last week Monday
    fun getLastWeekStartDate(): String {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        cal.add(Calendar.WEEK_OF_YEAR, -1)
        return dateFmt.format(cal.time)
    }

    // ✅ Get week plan
    fun getWeekPlan(
        companyCode: String,
        weekStartDate: String
    ): Flow<List<ShiftPlanEntity>> =
        shiftPlanDao.getWeekPlan(companyCode, weekStartDate)

    // ✅ Get employees for a shift this week
    suspend fun getShiftEmployees(
        companyCode: String,
        shiftCode: String,
        weekStartDate: String
    ): List<ShiftPlanEntity> =
        shiftPlanDao.getShiftEmployees(companyCode, shiftCode, weekStartDate)

    // ✅ Save shift plan
    suspend fun saveShiftPlan(
        btCode: String,
        companyCode: String,
        shiftCode: String,
        empIds: List<Pair<Long, String>>,
        weekStartDate: String,
        weekEndDate: String
    ) {
        shiftPlanDao.deleteShiftWeekPlan(companyCode, shiftCode, weekStartDate)

        val plans = empIds.map { (empId, empCode) ->
            ShiftPlanEntity(
                btCode        = btCode,
                companyCode   = companyCode,
                shiftCode     = shiftCode,
                empCode       = empCode,
                weekStartDate = weekStartDate,
                weekEndDate   = weekEndDate,
                createdAt     = dateFmt.format(Calendar.getInstance().time),
                updatedAt     = dateFmt.format(Calendar.getInstance().time)
            )
        }
        shiftPlanDao.insertAll(plans)
    }

    // ✅ Copy last week plan to new week
    suspend fun copyLastWeekPlan(
        companyCode: String,
        newWeekStartDate: String,
        newWeekEndDate: String
    ): Boolean {
        val lastWeekStartDate = getLastWeekStartDate()
        val lastWeekPlan = shiftPlanDao.getLastWeekPlan(
            companyCode, lastWeekStartDate
        )
        if (lastWeekPlan.isEmpty()) return false

        shiftPlanDao.deleteWeekPlan(companyCode, newWeekStartDate)

        val newPlans = lastWeekPlan.map { plan ->
            plan.copy(
                id            = 0,
                weekStartDate = newWeekStartDate,
                weekEndDate   = newWeekEndDate,
                updatedAt     = dateFmt.format(Calendar.getInstance().time)
            )
        }
        shiftPlanDao.insertAll(newPlans)
        return true
    }

    // ✅ Check if week plan exists
    suspend fun hasWeekPlan(
        companyCode: String,
        weekStartDate: String
    ): Boolean =
        shiftPlanDao.getWeekPlanCount(companyCode, weekStartDate) > 0
    // ✅ Extract empId from entity
    suspend fun getAssignedEmpIds(
        companyCode: String,
        shiftCode: String,
        weekStartDate: String
    ): List<String> =
        shiftPlanDao.getAssignedEmpIds(
            companyCode, shiftCode
        ).map { it.empCode }

    // ✅ Get assigned emp codes for a shift
    suspend fun getAssignedEmpCodes(
        companyCode: String,
        shiftCode: String
    ): List<String> =
        shiftPlanDao.getAssignedEmpIds(
            companyCode, shiftCode
        ).map { it.empCode }

    // ✅ Get shift count for company
    suspend fun getShiftCount(companyCode: String): Int =
        shiftPlanDao.getShiftCount(companyCode)


    suspend fun updateDeviationShift(employeeCode :String,companyCode: String,shiftCode: String){
        shiftPlanDao.updateDeviationShift(employeeCode,companyCode,shiftCode)
    }
}