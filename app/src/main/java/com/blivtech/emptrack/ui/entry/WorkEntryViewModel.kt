package com.blivtech.emptrack.ui.entry

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.model.WorkEntry
import com.blivtech.emptrack.data.repository.ProductRepository
import com.blivtech.emptrack.data.repository.WorkEntryRepository
import com.blivtech.emptrack.utils.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WorkEntryViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val entryRepository: WorkEntryRepository
) : ViewModel() {

    private var btCode = ""
    private val scope = MutableStateFlow<Triple<String, Long, String>?>(null) // company, employee, date
    private var nextClientId = 1L

    // Products still come from the local master (Room) — only entries moved to API-only.
    @OptIn(ExperimentalCoroutinesApi::class)
    val products: StateFlow<List<ProductWithWorks>> =
        scope.filterNotNull()
            .flatMapLatest { (company, _, _) -> productRepository.observeProducts(company) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // The working set lives here in memory until Done is pressed.
    private val _entries = MutableStateFlow<List<WorkEntry>>(emptyList())
    val entries: StateFlow<List<WorkEntry>> = _entries.asStateFlow()

    val totals: StateFlow<Totals> =
        _entries.map { list -> Totals(list.sumOf { it.pieces }, list.sumOf { it.amount }) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Totals(0, 0.0))

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _sheet = MutableStateFlow<SheetTarget?>(null)
    val sheet: StateFlow<SheetTarget?> = _sheet.asStateFlow()

    private val _result = MutableStateFlow<Result?>(null)
    val result: StateFlow<Result?> = _result.asStateFlow()

    fun setScope(btCode: String, companyCode: String, employeeId: Long, date: String) {
        this.btCode = btCode
        this.scope.value = Triple(companyCode, employeeId, date)
        loadEntries()
    }

    private fun loadEntries() {
        val s = scope.value ?: return
        viewModelScope.launch {
            _loading.value = true
            when (val res = entryRepository.getEntries(s.first, s.second, s.third)) {
                is Resource.Success ->
                    _entries.value = res.data.orEmpty().map { it.copy(clientId = nextClientId++) }
                is Resource.Error ->
                    _result.value = Result.Fail(res.message ?: "Could not load entries")
                Resource.Loading -> Unit
            }
            _loading.value = false
        }
    }

    fun startAdd(product: ProductWithWorks) { _sheet.value = SheetTarget.Add(product) }
    fun startEdit(entry: WorkEntry) { _sheet.value = SheetTarget.Edit(entry) }
    fun clearSheet() { _sheet.value = null }
    fun consumeResult() { _result.value = null }

    /** In-memory upsert. Matches by product + work, else appends. No network here. */
    fun upsertEntry(
        productId: Long, productName: String, unit: String,
        workTypeId: Long, workName: String, rate: Double, pieces: Int
    ) {
        if (pieces <= 0) { _result.value = Result.Fail("Enter at least 1 piece"); return }
        val s = scope.value ?: return
        val list = _entries.value.toMutableList()
        val idx = list.indexOfFirst { it.productId == productId && it.workTypeId == workTypeId }
        val existing = if (idx >= 0) list[idx] else null
        val row = WorkEntry(
            clientId = existing?.clientId ?: nextClientId++,
            serverId = existing?.serverId,
            btCode = btCode, companyCode = s.first, entryDate = s.third, employeeId = s.second,
            productId = productId, productName = productName,
            workTypeId = workTypeId, workName = workName, unit = unit,
            rate = rate, pieces = pieces, amount = pieces * rate
        )
        if (idx >= 0) list[idx] = row else list.add(row)
        _entries.value = list
    }

    /** In-memory delete only. It disappears from the Done payload, so the server drops it. */
    fun deleteEntry(entry: WorkEntry) {
        _entries.value = _entries.value.filterNot { it.clientId == entry.clientId }
    }

    /** Done button: persist the whole working set to the server in one call. */
    fun saveAll() {
        val s = scope.value ?: return
        viewModelScope.launch {
            _saving.value = true
            val res = entryRepository.saveEntries(btCode, s.first, s.third, s.second, _entries.value)
            _saving.value = false
            _result.value = when (res) {
                is Resource.Success -> Result.Saved
                is Resource.Error -> Result.Fail(res.message ?: "Could not save")
                Resource.Loading -> Result.Saved
            }
        }
    }

    data class Totals(val pieces: Int, val amount: Double)

    sealed interface SheetTarget {
        data class Add(val product: ProductWithWorks) : SheetTarget
        data class Edit(val entry: WorkEntry) : SheetTarget
    }

    sealed interface Result {
        data object Saved : Result
        data class Fail(val message: String) : Result
    }
}