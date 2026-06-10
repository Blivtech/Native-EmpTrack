package com.blivtech.emptrack.ui.entry

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.data.repository.EntryRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEntryViewModel @Inject constructor(
    private val entryRepository: EntryRepository,
    private val employeeRepository: EmployeeRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    // ─── LiveData ───────────────────
    private val _employees = MutableLiveData<List<EmployeeEntity>>()
    val employees: LiveData<List<EmployeeEntity>> = _employees

    private val _saveState = MutableLiveData<Resource<Any>?>()
    val saveState: LiveData<Resource<Any>?> = _saveState

    // ─── State ──────────────────────
    var entryType   = EntryType.OVERTIME
    var entryMode   = EntryMode.SINGLE

    var selectedDate       = ""
    var selectedShiftCode  = ""
    var selectedShiftName  = ""
    var selectedRepayMonth = ""
    var selectedBonusType  = "Performance"
    var otHours            = 0.0
    var amount             = 0.0
    var remarks            = ""

    // ✅ Selected employees
    val selectedEmployees = mutableListOf<EmployeeEntity>()

    // ─── Load data ──────────────────
    fun loadEmployees(companyCode: String) {
        viewModelScope.launch {
            employeeRepository.getEmployees(companyCode)
                .collect { list ->
                    _employees.value = list.filter { it.status == 1 }
                }
        }
    }

    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()

    // ─── Employee selection ─────────
    fun selectEmployee(emp: EmployeeEntity) {
        if (entryMode == EntryMode.SINGLE) {
            selectedEmployees.clear()
            selectedEmployees.add(emp)
        } else {
            if (selectedEmployees.none { it.empCode == emp.empCode }) {
                selectedEmployees.add(emp)
            }
        }
    }

    fun removeEmployee(empCode: String) {
        selectedEmployees.removeAll { it.empCode == empCode }
    }

    fun isEmployeeSelected(empCode: String) =
        selectedEmployees.any { it.empCode == empCode }

    // ─── Validation ─────────────────
    fun validate(): String? {
        if (selectedDate.isEmpty()) return "Please select a date"
        if (selectedEmployees.isEmpty()) return "Please select employee(s)"
        if (entryType == EntryType.OVERTIME) {
            if (selectedShiftCode.isEmpty()) return "Please select a shift"
            if (otHours <= 0) return "Please enter OT hours"
        }
        if (amount <= 0) return "Please enter amount"
        return null
    }

    // ─── Save ───────────────────────
    fun save(btCode: String, companyCode: String) {
        val error = validate()
        if (error != null) {
            _saveState.value = Resource.Error(error)
            return
        }

        _saveState.value = Resource.Loading
        viewModelScope.launch {
            when (entryType) {
                EntryType.OVERTIME -> saveOvertimes(btCode, companyCode)
                EntryType.ADVANCE  -> saveAdvances(btCode, companyCode)
                EntryType.BONUS    -> saveBonuses(btCode, companyCode)
            }
        }
    }

    private suspend fun saveOvertimes(btCode: String, companyCode: String) {
        var allSuccess = true
        for (emp in selectedEmployees) {
            val req = OvertimeRequest(
                btCode      = btCode,
                companyCode = companyCode,
                empCode     = emp.empCode,
                otDate      = selectedDate,
                shiftCode   = selectedShiftCode,
                otHours     = otHours,
                otAmount    = amount,
                remarks     = remarks
            )
            val result = entryRepository.addOvertime(req)
            if (result is Resource.Error) { allSuccess = false; break }
        }
        _saveState.value = if (allSuccess)
            Resource.Success("Saved successfully")
        else
            Resource.Error("Failed to save some records")
    }

    private suspend fun saveAdvances(btCode: String, companyCode: String) {
        var allSuccess = true
        for (emp in selectedEmployees) {
            val req = AdvanceRequest(
                btCode      = btCode,
                companyCode = companyCode,
                empCode     = emp.empCode,
                requestDate = selectedDate,
                amount      = amount,
                repayMonth  = selectedRepayMonth,
                remarks     = remarks
            )
            val result = entryRepository.addAdvance(req)
            if (result is Resource.Error) { allSuccess = false; break }
        }
        _saveState.value = if (allSuccess)
            Resource.Success("Saved successfully")
        else
            Resource.Error("Failed to save some records")
    }

    private suspend fun saveBonuses(btCode: String, companyCode: String) {
        var allSuccess = true
        for (emp in selectedEmployees) {
            val req = BonusRequest(
                btCode      = btCode,
                companyCode = companyCode,
                empCode     = emp.empCode,
                bonusDate   = selectedDate,
                bonusType   = selectedBonusType,
                amount      = amount,
                remarks     = remarks
            )
            val result = entryRepository.addBonus(req)
            if (result is Resource.Error) { allSuccess = false; break }
        }
        _saveState.value = if (allSuccess)
            Resource.Success("Saved successfully")
        else
            Resource.Error("Failed to save some records")
    }

    fun resetState() { _saveState.value = null }

    // ─── Helpers ────────────────────
    fun getSaveButtonText(): String {
        val count = selectedEmployees.size
        val type = when (entryType) {
            EntryType.OVERTIME -> "Overtime"
            EntryType.ADVANCE  -> "Advance"
            EntryType.BONUS    -> "Bonus"
        }
        return if (count <= 1) "Save $type"
        else "Save $count $type records"
    }

    fun getSaveButtonColor(): Int = when (entryType) {
        EntryType.OVERTIME -> android.graphics.Color.parseColor("#27500A")
        EntryType.ADVANCE  -> android.graphics.Color.parseColor("#633806")
        EntryType.BONUS    -> android.graphics.Color.parseColor("#3C3489")
    }
}