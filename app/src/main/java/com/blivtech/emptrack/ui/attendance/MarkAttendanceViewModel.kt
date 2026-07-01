package com.blivtech.emptrack.ui.attendance

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.AttendanceRepository
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.data.repository.ShiftPlanRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MarkAttendanceViewModel @Inject constructor(
    private val attendanceRepository: AttendanceRepository,
    private val employeeRepository: EmployeeRepository,
    private val shiftPlanRepository: ShiftPlanRepository
) : ViewModel() {

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // ─────────────────────────────────────
    // LiveData
    // ─────────────────────────────────────
    private val _employees = MutableLiveData<List<EmployeeEntity>>()
    val employees: LiveData<List<EmployeeEntity>> = _employees

    private val _allCompanyEmployees = MutableLiveData<List<EmployeeEntity>>()
    val allCompanyEmployees: LiveData<List<EmployeeEntity>> = _allCompanyEmployees

    private val _existingAttendance = MutableLiveData<Resource<AttendanceResponse>>()
    val existingAttendance: LiveData<Resource<AttendanceResponse>> = _existingAttendance

    private val _yesterdayAttendance = MutableLiveData<Resource<AttendanceResponse>>()
    val yesterdayAttendance: LiveData<Resource<AttendanceResponse>> = _yesterdayAttendance

    // ✅ Make it nullable
    private val _submitState = MutableLiveData<Resource<AttendanceResponse>?>()
    val submitState: LiveData<Resource<AttendanceResponse>?> = _submitState

    private val _showDeviation = MutableLiveData<Boolean>()
    val showDeviation: LiveData<Boolean> = _showDeviation


    val statusMap = mutableMapOf<String, AttendanceDetailRequest>()
    val deviationEmpCodes = mutableSetOf<String>()


    fun loadEmployees(companyCode: String, shiftCode: String) {
        viewModelScope.launch {
            // ✅ Check shift count for company
            val shiftCount = shiftPlanRepository.getShiftCount(companyCode)

            employeeRepository.getEmployees(companyCode)
                .collect { allEmps ->
                    val activeEmps = allEmps.filter { it.status == 1 }
                    _allCompanyEmployees.value = activeEmps

                    if (shiftCount <= 1) {
                        // ✅ Single shift — show all employees
                        _employees.value = activeEmps
                        _showDeviation.value = false
                    } else {
                        // ✅ Multiple shifts — filter by shift plan
                        val assignedCodes = shiftPlanRepository
                            .getAssignedEmpCodes(companyCode, shiftCode)

                        _employees.value = if (assignedCodes.isEmpty()) {
                            activeEmps
                        } else {
                            activeEmps.filter { it.empCode in assignedCodes }
                        }
                        _showDeviation.value = true
                    }
                }
        }
    }

    // ─────────────────────────────────────
    // ✅ Load existing attendance (EDIT)
    // ─────────────────────────────────────
    fun loadExistingAttendance(attendanceId: String) {
        _existingAttendance.value = Resource.Loading
        viewModelScope.launch {
            _existingAttendance.value =
                attendanceRepository.getAttendanceById(attendanceId)
        }
    }

    // ─────────────────────────────────────
    // ✅ Load yesterday attendance (prefill)
    // ─────────────────────────────────────
    fun loadYesterdayAttendance(
        btCode: String,
        companyCode: String,
        shiftCode: String
    ) {
        viewModelScope.launch {
            val yesterday = Calendar.getInstance().apply {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            val yesterdayStr = dateFmt.format(yesterday.time)
            val attendanceId = "ATT-$btCode-${yesterdayStr.replace("-", "")}-$shiftCode"

            _yesterdayAttendance.value = Resource.Loading
            _yesterdayAttendance.value =
                attendanceRepository.getAttendanceById(attendanceId)
        }
    }

    // ─────────────────────────────────────
    // ✅ Prefill from yesterday
    // ─────────────────────────────────────
    fun prefillFromYesterday() {
        val yesterday = _yesterdayAttendance.value
        if (yesterday is Resource.Success) {
            val att = yesterday.data
            if (att.isMarked && att.employees != null) {
                att.employees.forEach { detail ->
                    // ✅ Only prefill if employee exists in current list
                    val exists = _employees.value?.any {
                        it.empCode == detail.empCode
                    } == true
                    if (exists) {
                        updateStatus(
                            empCode       = detail.empCode,
                            dayPlanStatus = detail.dayPlanStatus,
                            workType      = detail.workType,
                            remarks       = detail.remarks ?: ""
                        )
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Add deviation employees
    // ─────────────────────────────────────
    fun addDeviationEmployees(empCodes: List<String>) {
        val allEmps = _allCompanyEmployees.value ?: return
        val currentList = _employees.value?.toMutableList() ?: mutableListOf()

        empCodes.forEach { empCode ->
            deviationEmpCodes.add(empCode)
            val emp = allEmps.find { it.empCode == empCode }
            if (emp != null && currentList.none { it.empCode == empCode }) {
                currentList.add(emp)
            }
        }
        _employees.value = currentList
    }

    // ─────────────────────────────────────
    // ✅ Update employee status
    // ─────────────────────────────────────
    fun updateStatus(
        empCode: String,
        dayPlanStatus: Int,
        workType: Int = 1,
        remarks: String = ""
    ) {
        val presentCount = when {
            dayPlanStatus == 1 && workType == 1 -> 1.0
            dayPlanStatus == 1 && workType == 2 -> 0.5
            else -> 0.0
        }
        val absentCount  = if (dayPlanStatus == 3) 1 else 0
        val isDeviation  = empCode in deviationEmpCodes

        statusMap[empCode] = AttendanceDetailRequest(
            empCode       = empCode,
            dayPlanStatus = dayPlanStatus,
            workType      = workType,
            presentCount  = presentCount,
            absentCount   = absentCount,
            remarks       = remarks,
            isDeviation   = isDeviation
        )
    }

    // ─────────────────────────────────────
    // ✅ Helper functions
    // ─────────────────────────────────────
    fun allMarked()     = statusMap.size == (_employees.value?.size ?: 0)
    fun pendingCount()  = (_employees.value?.size ?: 0) - statusMap.size
    fun getCount(s: Int) = statusMap.values.count { it.dayPlanStatus == s }

    fun getDeviationEmployees(): List<EmployeeEntity> {
        val allEmps = _allCompanyEmployees.value ?: return emptyList()
        val currentCodes = _employees.value?.map { it.empCode }?.toSet() ?: emptySet()
        return allEmps.filter { it.empCode !in currentCodes }
    }

    fun resetSaveState() {
        _submitState.value = null
    }

    // ─────────────────────────────────────
    // ✅ Submit attendance
    // ─────────────────────────────────────
    fun submit(
        mode: String,
        attendanceId: String?,
        btCode: String,
        companyCode: String,
        shiftCode: String,
        date: String,
        markedBy: Long
    ) {
        _submitState.value = Resource.Loading
        viewModelScope.launch {
            val request = AttendanceRequest(
                btCode         = btCode,
                companyCode    = companyCode,
                shiftCode      = shiftCode,
                attendanceDate = date,
                markedBy       = markedBy,
                employees      = statusMap.values.toList()
            )
            _submitState.value = if (mode == "EDIT" && attendanceId != null) {
                attendanceRepository.updateAttendance(attendanceId, request)
            } else {
                attendanceRepository.markAttendance(request)
            }
        }
    }
}