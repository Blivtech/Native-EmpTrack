package com.blivtech.emptrack.ui.contract

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.ContractEntryEntity
import com.blivtech.emptrack.data.local.entity.ContractProductEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.data.repository.CompanyRepository
import com.blivtech.emptrack.utils.Resource
import com.emptrack.data.repository.ContractWageRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ContractWageViewModel @Inject constructor(
    private val contractWageRepository: ContractWageRepository,
    private val companyRepository: CompanyRepository
) : ViewModel() {

    // ─────────────────────────────────
    // ✅ State
    // ─────────────────────────────────
    private val _saveState = MutableLiveData<Resource<Unit>?>()
    val saveState: LiveData<Resource<Unit>?> = _saveState

    private val _deleteState = MutableLiveData<Resource<Unit>?>()
    val deleteState: LiveData<Resource<Unit>?> = _deleteState

    // ✅ Entry rows for Add Entry screen
    private val _entryRows = MutableLiveData<List<ContractEntryRow>>(emptyList())
    val entryRows: LiveData<List<ContractEntryRow>> = _entryRows

    // ✅ Selected shift
    var selectedShiftCode = ""
    var selectedShiftName = ""
    var selectedShiftTime = ""

    // ✅ Selected date
    var selectedDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    var selectedDisplayDate = SimpleDateFormat("d MMM yyyy", Locale.getDefault()).format(Date())
    val isToday get() = selectedDate == SimpleDateFormat(
        "yyyy-MM-dd", Locale.getDefault()
    ).format(Date())

    // ─────────────────────────────────
    // ✅ Products
    // ─────────────────────────────────

    fun getProducts(btCode: String, companyCode: String) =
        contractWageRepository.getProducts(btCode, companyCode).asLiveData()

    fun addProduct(req: ContractProductRequest) {
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            _saveState.value = when (val r = contractWageRepository.addProduct(req)) {
                is Resource.Success -> Resource.Success(Unit)
                is Resource.Error   -> Resource.Error(r.message)
                else                -> Resource.Error("Unknown error")
            }
        }
    }

    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            contractWageRepository.deleteProduct(productId)
        }
    }

    // ─────────────────────────────────
    // ✅ Units
    // ─────────────────────────────────

    fun getUnits(btCode: String) =
        contractWageRepository.getUnits(btCode).asLiveData()

    fun addUnit(btCode: String, unitName: String) {
        viewModelScope.launch {
            contractWageRepository.addUnit(btCode, unitName)
        }
    }

    fun initDefaultUnits(btCode: String) {
        viewModelScope.launch {
            contractWageRepository.insertDefaultUnitsIfNeeded(btCode)
        }
    }

    // ─────────────────────────────────
    // ✅ Shifts from active company
    // ─────────────────────────────────

    fun getShifts(companyCode: String) =
        companyRepository.getShiftsByCompany(companyCode).asLiveData()

    fun autoDetectShift(shifts: List<ShiftEntity>) {
        if (shifts.isEmpty()) return
        val now = Calendar.getInstance()
        val currentHour = now.get(Calendar.HOUR_OF_DAY)
        val currentMin  = now.get(Calendar.MINUTE)
        val currentMins = currentHour * 60 + currentMin

        val matched = shifts.firstOrNull { shift ->
            val start = shift.startTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            val end   = shift.endTime.split(":").let { it[0].toInt() * 60 + it[1].toInt() }
            if (end > start) currentMins in start..end
            else currentMins >= start || currentMins <= end
        } ?: shifts.first()

        selectedShiftCode = matched.shiftCode
        selectedShiftName = matched.shiftName
        selectedShiftTime = "${matched.startTime.take(5)} – ${matched.endTime.take(5)}"
    }

    // ─────────────────────────────────
    // ✅ Entry rows management
    // ─────────────────────────────────

    fun addRow() {
        val current = _entryRows.value?.toMutableList() ?: mutableListOf()
        current.add(ContractEntryRow())
        _entryRows.value = current
    }

    fun updateRow(index: Int, row: ContractEntryRow) {
        val current = _entryRows.value?.toMutableList() ?: return
        if (index < current.size) {
            current[index] = row
            _entryRows.value = current
        }
    }

    fun removeRow(index: Int) {
        val current = _entryRows.value?.toMutableList() ?: return
        if (index < current.size) {
            current.removeAt(index)
            _entryRows.value = current
        }
    }

    fun getTotalAmount(): Double =
        _entryRows.value?.sumOf { it.totalAmount } ?: 0.0

    fun getFilledRows(): List<ContractEntryRow> =
        _entryRows.value?.filter {
            it.productId.isNotEmpty() && it.quantityDone > 0
        } ?: emptyList()

    // ─────────────────────────────────
    // ✅ Save entries
    // ─────────────────────────────────

    fun saveEntries(btCode: String, companyCode: String) {
        val rows = getFilledRows()
        if (rows.isEmpty()) {
            _saveState.value = Resource.Error("Add at least one entry")
            return
        }
        _saveState.value = Resource.Loading
        viewModelScope.launch {
            val req = ContractEntryRequest(
                btCode      = btCode,
                companyCode = companyCode,
                shiftCode   = selectedShiftCode,
                shiftName   = selectedShiftName,
                entryDate   = selectedDate,
                entries     = rows.map {
                    ContractEntryDetail(
                        productId    = it.productId,
                        productName  = it.productName,
                        workName     = it.workName,
                        quantityDone = it.quantityDone,
                        ratePerUnit  = it.ratePerUnit,
                        totalAmount  = it.totalAmount,
                        unit         = it.unit
                    )
                }
            )
            _saveState.value = contractWageRepository.saveEntries(req)
        }
    }

    // ─────────────────────────────────
    // ✅ Entries
    // ─────────────────────────────────

    fun getEntriesByMonth(btCode: String, companyCode: String, month: String) =
        contractWageRepository.getEntriesByMonth(btCode, companyCode, month).asLiveData()

    fun deleteEntry(entryId: String) {
        viewModelScope.launch {
            _deleteState.value = contractWageRepository.deleteEntry(entryId)
        }
    }

    fun resetSaveState() { _saveState.value = null }
    fun resetDeleteState() { _deleteState.value = null }

    // ─────────────────────────────────
    // ✅ Monthly summary
    // ─────────────────────────────────

    fun buildMonthlySummary(
        entries: List<ContractEntryEntity>
    ): List<ContractSummaryItem> {
        return entries
            .groupBy { "${it.productId}_${it.workName}" }
            .map { (_, group) ->
                val first = group.first()
                ContractSummaryItem(
                    productName  = first.productName,
                    workName     = first.workName,
                    totalQty     = group.sumOf { it.quantityDone },
                    ratePerUnit  = first.ratePerUnit,
                    totalAmount  = group.sumOf { it.totalAmount },
                    unit         = first.unit
                )
            }
    }
    // ✅ Add to ContractWageViewModel
    fun updateProduct(product: com.blivtech.emptrack.data.local.entity.ContractProductEntity) {
        viewModelScope.launch {
            contractWageRepository.updateProduct(product)
        }
    }

    fun currentMonth(): String =
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
}