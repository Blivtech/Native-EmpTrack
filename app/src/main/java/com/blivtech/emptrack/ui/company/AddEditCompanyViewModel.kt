package com.blivtech.emptrack.ui.company

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.CompanyEntity
import com.blivtech.emptrack.data.model.CompanyRequest
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddEditCompanyViewModel @Inject constructor(
    private val repository: CompanyRepository
) : ViewModel() {

    private val _saveState = MutableLiveData<Resource<CompanyEntity>>()
    val saveState: LiveData<Resource<CompanyEntity>> = _saveState

    // ✅ Shift list managed in ViewModel
    private val _shifts = MutableLiveData<MutableList<CompanyRequest.ShiftRequest>>(mutableListOf())
    val shifts: LiveData<MutableList<CompanyRequest.ShiftRequest>> = _shifts

    fun addShift(shift: CompanyRequest.ShiftRequest) {
        val list = _shifts.value ?: mutableListOf()
        list.add(shift)
        _shifts.value = list
    }

    fun removeShift(index: Int) {
        val list = _shifts.value ?: mutableListOf()
        if (index < list.size) {
            list.removeAt(index)
            _shifts.value = list
        }
    }

    // ✅ Pre-fill for edit
    fun setShifts(shifts: List<CompanyRequest.ShiftRequest>) {
        _shifts.value = shifts.toMutableList()
    }

    fun createCompany(request: CompanyRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.createCompany(request)
        }
    }

    fun updateCompany(id: Long, request: CompanyRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.updateCompany(id, request)
        }
    }
}