package com.blivtech.emptrack.ui.employee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EmployeeListViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _deleteState = MutableLiveData<Resource<Any>>()
    val deleteState: LiveData<Resource<Any>> = _deleteState

    // ✅ Single employee LiveData
    private val _employee = MutableLiveData<EmployeeEntity?>()
    val employee: LiveData<EmployeeEntity?> = _employee

    fun getEmployees(companyId: String) =
        repository.getEmployees(companyId).asLiveData()

    // ✅ Load single employee into LiveData
    fun loadEmployeeById(id: Long) {
        viewModelScope.launch {
            _employee.value = repository.getEmployeeById(id)
        }
    }

    fun deleteEmployee(id: Long) {
        _deleteState.value = Resource.Loading
        viewModelScope.launch {
            _deleteState.value = repository.deleteEmployee(id)
        }
    }
}