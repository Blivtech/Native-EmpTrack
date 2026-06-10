package com.blivtech.emptrack.ui.work

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.WorkRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddWorkEntryViewModel @Inject constructor(
    private val repository: WorkRepository
) : ViewModel() {

    // ✅ Products loaded from API
    private val _products = MutableLiveData<List<ProductResponse>>()
    val products: LiveData<List<ProductResponse>> = _products

    // ✅ Save state
    private val _saveState = MutableLiveData<Resource<String>>()
    val saveState: LiveData<Resource<String>> = _saveState

    // ✅ Entry rows (dynamic)
    private val _rows = MutableLiveData<MutableList<WorkEntryRow>>(
        mutableListOf(WorkEntryRow(id = 0))  // start with 1 empty row
    )
    val rows: LiveData<MutableList<WorkEntryRow>> = _rows

    // ✅ Selected employee
    var selectedEmpCode = ""
    var selectedEmpName = ""

    // ✅ Selected date
    var selectedDate = ""

    // ─────────────────────────────────────
    // ✅ Load products
    // ─────────────────────────────────────
    fun loadProducts(btCode: String, companyCode: String) {
        viewModelScope.launch {
            val result = repository.getProducts(btCode, companyCode)
            if (result is Resource.Success) {
                _products.value = result.data
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Row management
    // ─────────────────────────────────────
    fun addRow() {
        val list = _rows.value ?: mutableListOf()
        list.add(WorkEntryRow(id = list.size))
        _rows.value = list
    }

    fun removeRow(index: Int) {
        val list = _rows.value ?: return
        if (list.size > 1 && index < list.size) {
            list.removeAt(index)
            _rows.value = list
        }
    }

    fun updateRow(index: Int, row: WorkEntryRow) {
        val list = _rows.value ?: return
        if (index < list.size) {
            list[index] = row
            _rows.value = list
        }
    }

    // ─────────────────────────────────────
    // ✅ Calc total
    // ─────────────────────────────────────
    fun getTotalAmount(): Double =
        (_rows.value ?: emptyList())
            .filter { it.isValid }
            .sumOf { it.totalAmount }

    fun getValidRowCount(): Int =
        (_rows.value ?: emptyList())
            .count { it.isValid }

    // ─────────────────────────────────────
    // ✅ Save all entries
    // ─────────────────────────────────────
    fun saveEntries(btCode: String, companyCode: String) {
        val validRows = (_rows.value ?: emptyList()).filter { it.isValid }
        if (validRows.isEmpty()) {
            _saveState.value = Resource.Error("No valid entries to save")
            return
        }

        val request = WorkEntryRequest(
            btCode      = btCode,
            companyCode = companyCode,
            empCode     = selectedEmpCode,
            entryDate   = selectedDate,
            entries     = validRows.map { row ->
                WorkEntryDetail(
                    productId     = row.productId,
                    workTypeId    = row.workTypeId,
                    piecesDone    = row.piecesDone,
                    ratePerPiece  = row.ratePerPiece,
                    totalAmount   = row.totalAmount,
                    remarks       = null
                )
            }
        )

        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = repository.addWorkEntry(request)
        }
    }

    // ─────────────────────────────────────
    // ✅ Get work types for a product
    // ─────────────────────────────────────
    fun getWorkTypesForProduct(productId: String): List<WorkTypeResponse> =
        _products.value
            ?.find { it.productId == productId }
            ?.workTypes
            ?: emptyList()
}