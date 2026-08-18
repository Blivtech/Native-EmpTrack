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



    private val _deptCreated = MutableLiveData<DepartmentEntity?>()
    val deptCreated: LiveData<DepartmentEntity?> = _deptCreated


    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

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

    fun updateEmployee(editEmployeeCode: String, request: EmployeeRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.updateEmployee(editEmployeeCode, request)
        }
    }


    // ✅ Create department on the fly
    fun createDepartment(btCode: String, name: String, desc: String) {
        viewModelScope.launch {
            when (val result = repository.createDepartment(btCode, name, desc)) {
                is Resource.Success -> _deptCreated.value = result.data
                is Resource.Error   -> _errorMessage.value = result.message
                Resource.Loading -> TODO()
            }
        }
    }

    // ✅ Create designation on the fly
    fun createDesignation(btCode: String, name: String, desc: String) {
        viewModelScope.launch {
            when (val result = repository.createDesignation(btCode, name, desc)) {
                is Resource.Success -> _desgCreated.value = result.data
                is Resource.Error   -> _errorMessage.value = result.message
                Resource.Loading -> TODO()
            }
        }
    }

}