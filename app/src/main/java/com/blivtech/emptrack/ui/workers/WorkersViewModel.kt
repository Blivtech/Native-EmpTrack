package com.blivtech.emptrack.ui.workers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.dao.EmployeeDao
import com.blivtech.emptrack.data.model.DayTotalDto
import com.blivtech.emptrack.data.repository.WorkEntryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class WorkersViewModel @Inject constructor(
    private val employeeDao: EmployeeDao,
    private val entryRepository: WorkEntryRepository
) : ViewModel() {

    private val company = MutableStateFlow<String?>(null)
    private val _date = MutableStateFlow(today())
    val date: StateFlow<String> = _date.asStateFlow()

    // Bumping this re-fetches day totals (e.g. after returning from the entry screen).
    private val refreshTrigger = MutableStateFlow(0)

    // Employees still come from the local master (Room).
    @OptIn(ExperimentalCoroutinesApi::class)
    private val employees =
        company.filterNotNull().flatMapLatest { employeeDao.getEmployeesByCompanys(it) }

    // Day totals come from the API.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val dayTotals: Flow<List<DayTotalDto>> =
        combine(company.filterNotNull(), _date, refreshTrigger) { c, d, _ -> c to d }
            .mapLatest { (c, d) -> entryRepository.getDayTotals(c, d) }

    val workers: StateFlow<List<WorkerRow>> =
        combine(employees, dayTotals) { emps, totals ->
            val byEmp = totals.associateBy { it.employeeId }
            emps.map { e ->
                val t = byEmp[e.id]
                WorkerRow(
                    id = e.id,
                    name = e.name,
                    role = e.desgName ?: "",
                    pieces = t?.pieces ?: 0,
                    amount = t?.amount ?: 0.0
                )
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setCompany(companyCode: String) { company.value = companyCode }
    fun setDate(date: String) { _date.value = date }
    fun refresh() { refreshTrigger.value++ }

    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    data class WorkerRow(
        val id: Long,
        val name: String,
        val role: String,
        val pieces: Int,
        val amount: Double
    )
}