package com.blivtech.emptrack.ui.report

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.MonthlyEmployeeDetail
import com.blivtech.emptrack.data.repository.ReportRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MonthlyEmployeeDetailViewModel @Inject constructor(
    private val repository: ReportRepository
) : ViewModel() {

    private val _detail = MutableLiveData<MonthlyEmployeeDetail?>()
    val detail: LiveData<MonthlyEmployeeDetail?> = _detail

    private val _loading = MutableLiveData(false)
    val loading: LiveData<Boolean> = _loading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    fun loadDetail(
        btCode: String, companyCode: String,
        month: String, shiftCode: String, empCode: String
    ) {
        viewModelScope.launch {
            repository.getMonthlyEmployeeDetail(
                btCode, companyCode, month, shiftCode, empCode
            ).collect { resource ->
                when (resource) {
                    is Resource.Loading -> _loading.value = true
                    is Resource.Success -> {
                        _loading.value = false
                        _detail.value  = resource.data
                    }
                    is Resource.Error -> {
                        _loading.value = false
                        _error.value   = resource.message
                    }
                }
            }
        }
    }

    fun resetError() { _error.value = null }
}