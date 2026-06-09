package com.blivtech.emptrack.ui.employee

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.DepartmentEntity
import com.blivtech.emptrack.data.local.entity.DesignationEntity
import com.blivtech.emptrack.data.local.entity.EmployeeEntity
import com.blivtech.emptrack.data.model.EmployeeRequest
import com.blivtech.emptrack.data.repository.EmployeeRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditEmployeeViewModel @Inject constructor(
    private val repository: EmployeeRepository
) : ViewModel() {

    private val _saveState = MutableLiveData<Resource<EmployeeEntity>>()
    val saveState: LiveData<Resource<EmployeeEntity>> = _saveState

    // ✅ New dept/desg created
    private val _deptCreated = MutableLiveData<DepartmentEntity?>()
    val deptCreated: LiveData<DepartmentEntity?> = _deptCreated

    private val _desgCreated = MutableLiveData<DesignationEntity?>()
    val desgCreated: LiveData<DesignationEntity?> = _desgCreated

    fun getDepartments(btCode: String) =
        repository.getDepartments(btCode).asLiveData()

    fun getDesignations(btCode: String) =
        repository.getDesignations(btCode).asLiveData()

    fun createEmployee(request: EmployeeRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.createEmployee(request)
        }
    }

    fun updateEmployee(id: Long, request: EmployeeRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.updateEmployee(id, request)
        }
    }

    // ✅ Create department on the fly
    fun createDepartment(btCode: String, name: String, desc: String) {
        viewModelScope.launch {
            val entity = repository.createDepartment(btCode, name, desc)
            _deptCreated.value = entity
        }
    }

    // ✅ Create designation on the fly
    fun createDesignation(btCode: String, name: String, desc: String) {
        viewModelScope.launch {
            val entity = repository.createDesignation(btCode, name, desc)
            _desgCreated.value = entity
        }
    }
}