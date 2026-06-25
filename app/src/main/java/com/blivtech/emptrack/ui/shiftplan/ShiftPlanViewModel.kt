package com.blivtech.emptrack.ui.shiftplan

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.local.entity.ShiftPlanEntity
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.data.repository.ShiftPlanRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ShiftPlanViewModel @Inject constructor(
    private val shiftPlanRepository: ShiftPlanRepository,
    private val employeeRepository: EmployeeRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    // ─────────────────────────────────────
    // LiveData
    // ─────────────────────────────────────

    // ✅ ALL shift assignments for the selected week — across ALL shifts
    private val _weekPlan = MutableLiveData<List<ShiftPlanEntity>>()
    val weekPlan: LiveData<List<ShiftPlanEntity>> = _weekPlan

    // ✅ ALL active employees for the company
    private val _employees = MutableLiveData<List<EmployeeEntity>>()
    val employees: LiveData<List<EmployeeEntity>> = _employees

    private val _saveState = MutableLiveData<Boolean>()
    val saveState: LiveData<Boolean> = _saveState

    private val _hasLastWeekPlan = MutableLiveData<Boolean>()
    val hasLastWeekPlan: LiveData<Boolean> = _hasLastWeekPlan

    // ─────────────────────────────────────
    // ✅ Calendar for week navigation
    // ─────────────────────────────────────
    private val dateFmt    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("d MMM", Locale.getDefault())

    private var selectedCal: Calendar = Calendar.getInstance().apply {
        set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
    }

    private var currentCompanyCode = ""

    // ─────────────────────────────────────
    // ✅ Week helpers
    // ─────────────────────────────────────
    fun getSelectedWeekStart(): String {
        val cal = selectedCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFmt.format(cal.time)
    }

    fun getSelectedWeekEnd(): String {
        val cal = selectedCal.clone() as Calendar
        cal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        cal.add(Calendar.WEEK_OF_YEAR, 1)
        return dateFmt.format(cal.time)
    }

    fun getWeekLabel(): String {
        val start = selectedCal.clone() as Calendar
        start.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val end = selectedCal.clone() as Calendar
        end.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        end.add(Calendar.WEEK_OF_YEAR, 1)

        val year = start.get(Calendar.YEAR)
        return "${displayFmt.format(start.time)} – ${displayFmt.format(end.time)} $year"
    }

    fun getWeekNumber(): String {
        val weekNum = selectedCal.get(Calendar.WEEK_OF_YEAR)
        val year    = selectedCal.get(Calendar.YEAR)
        return "Week $weekNum · $year"
    }

    fun isCurrentWeek(): Boolean {
        val current = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        }
        val selected = selectedCal.clone() as Calendar
        selected.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        return dateFmt.format(current.time) == dateFmt.format(selected.time)
    }

    // ─────────────────────────────────────
    // ✅ Week navigation
    // ─────────────────────────────────────
    fun previousWeek() {
        selectedCal.add(Calendar.WEEK_OF_YEAR, -1)
        refreshWeekPlan()
    }

    fun nextWeek() {
        selectedCal.add(Calendar.WEEK_OF_YEAR, 1)
        refreshWeekPlan()
    }

    private fun refreshWeekPlan() {
        if (currentCompanyCode.isNotEmpty()) {
            loadWeekPlan(currentCompanyCode)
        }
    }

    // ─────────────────────────────────────
    // ✅ Data loading
    // ─────────────────────────────────────
    fun getCompanies(btCode: String) =
        companyRepository.getCompanies().asLiveData()

    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()

    fun loadEmployees(companyCode: String) {
        currentCompanyCode = companyCode
        viewModelScope.launch {
            employeeRepository.getEmployees(companyCode)
                .collect { list ->
                    _employees.value = list.filter { it.status == 1 }
                }
        }
    }

    // ✅ Loads ALL shift assignments for the WHOLE company, for the selected week
    // (not filtered by shiftCode — that's done in-memory by getX() helpers below)
    fun loadWeekPlan(companyCode: String) {
        currentCompanyCode = companyCode
        viewModelScope.launch {
            shiftPlanRepository
                .getWeekPlan(companyCode, getSelectedWeekStart())
                .collect { plans ->
                    _weekPlan.value = plans
                }
        }
    }

    // ─────────────────────────────────────
    // ✅ Save + Copy
    // ─────────────────────────────────────

    // ✅ Saves the FULL assigned list for one shift (replace, not append)
    fun saveShiftPlan(
        btCode: String,
        companyCode: String,
        shiftCode: String,
        assignedEmployees: List<EmployeeEntity>
    ) {
        viewModelScope.launch {
            shiftPlanRepository.saveShiftPlan(
                btCode        = btCode,
                companyCode   = companyCode,
                shiftCode     = shiftCode,
                empIds        = assignedEmployees.map { Pair(it.id, it.empCode) },
                weekStartDate = getSelectedWeekStart(),
                weekEndDate   = getSelectedWeekEnd()
            )
            // ✅ Reload full week plan so other shift screens see the update immediately
            loadWeekPlan(companyCode)
            _saveState.value = true
        }
    }

    fun copyLastWeekPlan(companyCode: String) {
        viewModelScope.launch {
            val success = shiftPlanRepository.copyLastWeekPlan(
                companyCode      = companyCode,
                newWeekStartDate = getSelectedWeekStart(),
                newWeekEndDate   = getSelectedWeekEnd()
            )
            loadWeekPlan(companyCode)
            _saveState.value = success
        }
    }

    fun checkLastWeekPlan(companyCode: String) {
        viewModelScope.launch {
            val lastWeekStart = shiftPlanRepository.getLastWeekStartDate()
            _hasLastWeekPlan.value = shiftPlanRepository
                .hasWeekPlan(companyCode, lastWeekStart)
        }
    }

    // ─────────────────────────────────────
    // ✅ Shift Plan overview card helpers
    // ─────────────────────────────────────
    fun getShiftEmpCount(shiftCode: String): Int =
        _weekPlan.value?.count { it.shiftCode == shiftCode } ?: 0

    fun getShiftEmpNames(shiftCode: String): List<String> {
        val planItems = _weekPlan.value
            ?.filter { it.shiftCode == shiftCode }
            ?: return emptyList()
        val allEmps = _employees.value ?: emptyList()
        return planItems.take(4).map { plan ->
            allEmps.find { it.empCode == plan.empCode }
                ?.name?.split(" ")?.first()
                ?: plan.empCode
        }
    }

    fun getUnassignedCount(): Int {
        val allEmps     = _employees.value ?: return 0
        val assignedIds = _weekPlan.value?.map { it.empCode }?.toSet() ?: emptySet()
        return allEmps.count { it.empCode !in assignedIds }
    }

    // ─────────────────────────────────────
    // ✅ ★ CORE RULE — used by AssignShiftActivity ★
    // One employee = one shift per week.
    // ─────────────────────────────────────

    /**
     * Employees currently assigned to THIS shift.
     */
    fun getAssignedEmployees(shiftCode: String): List<EmployeeEntity> {
        val allEmps = _employees.value ?: return emptyList()
        val plan    = _weekPlan.value  ?: return emptyList()

        val assignedCodes = plan
            .filter { it.shiftCode == shiftCode }
            .map { it.empCode }
            .toSet()

        return allEmps.filter { it.empCode in assignedCodes }
    }

    /**
     * Employees that CAN be added to this shift —
     * i.e. NOT assigned to this shift AND NOT assigned to ANY other shift this week.
     */
    fun getAvailableEmployees(shiftCode: String): List<EmployeeEntity> {
        val allEmps = _employees.value ?: return emptyList()
        val plan    = _weekPlan.value  ?: return emptyList()

        // ✅ Every empCode that has ANY assignment this week (any shift)
        val assignedAnywhere = plan
            .map { it.empCode }
            .toSet()

        return allEmps.filter { it.empCode !in assignedAnywhere }
    }

    fun resetSaveState() {
        _saveState.value = false
    }
}