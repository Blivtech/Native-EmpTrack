package com.emptrack.data.repository


import com.blivtech.emptrack.data.local.dao.ContractEntryDao
import com.blivtech.emptrack.data.local.dao.UnitDao
import com.blivtech.emptrack.data.local.entity.ContractEntryEntity
import com.blivtech.emptrack.data.local.entity.ContractProductDao
import com.blivtech.emptrack.data.local.entity.ContractProductEntity
import com.blivtech.emptrack.data.local.entity.UnitEntity
import com.blivtech.emptrack.data.model.ContractEntryRequest
import com.blivtech.emptrack.data.model.ContractProductRequest
import com.blivtech.emptrack.data.network.ApiService
import com.blivtech.emptrack.utils.Resource
import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContractWageRepository @Inject constructor(
    private val contractProductDao: ContractProductDao,
    private val contractEntryDao: ContractEntryDao,
    private val unitDao: UnitDao,
    private val apiService: ApiService
) {

    // ─────────────────────────────────
    // ✅ Products
    // ─────────────────────────────────

    fun getProducts(btCode: String, companyCode: String): Flow<List<ContractProductEntity>> =
        contractProductDao.getProducts(btCode, companyCode)

    suspend fun addProduct(req: ContractProductRequest): Resource<ContractProductEntity> {
        return try {
            val productId = "CP-${req.btCode}-${System.currentTimeMillis()}"
            val entity = ContractProductEntity(
                productId   = productId,
                btCode      = req.btCode,
                companyCode = req.companyCode,
                productName = req.productName,
                workName    = req.workName,
                ratePerUnit = req.ratePerUnit,
                unit        = req.unit,
                colorTag    = req.colorTag,
                status      = 1
            )
            contractProductDao.insert(entity)

            // ✅ Also sync to API
            try {
                apiService.addContractProduct(req)
            } catch (e: Exception) {
                // API fails silently — local save is enough
            }

            Resource.Success(entity)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save product")
        }
    }

    suspend fun updateProduct(product: ContractProductEntity): Resource<Unit> {
        return try {
            contractProductDao.update(product)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update")
        }
    }

    suspend fun deleteProduct(productId: String): Resource<Unit> {
        return try {
            contractProductDao.deleteProduct(productId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete")
        }
    }

    // ─────────────────────────────────
    // ✅ Units (local only)
    // ─────────────────────────────────

    fun getUnits(btCode: String): Flow<List<UnitEntity>> =
        unitDao.getUnits(btCode)

    suspend fun addUnit(btCode: String, unitName: String) {
        val count = unitDao.getUnitCount(btCode)
        if (count == 0) {
            // ✅ Insert defaults if first time
            insertDefaultUnits(btCode)
        }
        unitDao.insert(
            UnitEntity(
                btCode    = btCode,
                unitName  = unitName,
                createdAt = SimpleDateFormat(
                    "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
                ).format(Date())
            )
        )
    }

    suspend fun insertDefaultUnitsIfNeeded(btCode: String) {
        val count = unitDao.getUnitCount(btCode)
        if (count == 0) insertDefaultUnits(btCode)
    }

    private suspend fun insertDefaultUnits(btCode: String) {
        val now = SimpleDateFormat(
            "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
        ).format(Date())
        val defaults = listOf("kg", "mtr", "pcs", "bundle", "ltr", "ton")
        unitDao.insertAll(defaults.map { UnitEntity(btCode = btCode, unitName = it, createdAt = now) })
    }

    suspend fun deleteUnit(id: Int) = unitDao.deleteById(id)

    // ─────────────────────────────────
    // ✅ Entries
    // ─────────────────────────────────

    fun getEntriesByMonth(
        btCode: String, companyCode: String, month: String
    ): Flow<List<ContractEntryEntity>> =
        contractEntryDao.getEntriesByMonth(btCode, companyCode, month)

    suspend fun getEntriesByDateShift(
        btCode: String, companyCode: String,
        date: String, shiftCode: String
    ): List<ContractEntryEntity> =
        contractEntryDao.getEntriesByDateShift(btCode, companyCode, date, shiftCode)

    suspend fun saveEntries(req: ContractEntryRequest): Resource<Unit> {
        return try {
            val now = SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss", Locale.getDefault()
            ).format(Date())

            req.entries.forEachIndexed { index, detail ->
                val entryId = "CE-${req.btCode}-${req.entryDate.replace("-","")}-${req.shiftCode}-${index + 1}-${System.currentTimeMillis()}"
                val entity = ContractEntryEntity(
                    entryId      = entryId,
                    btCode       = req.btCode,
                    companyCode  = req.companyCode,
                    shiftCode    = req.shiftCode,
                    shiftName    = req.shiftName,
                    entryDate    = req.entryDate,
                    productId    = detail.productId,
                    productName  = detail.productName,
                    workName     = detail.workName,
                    quantityDone = detail.quantityDone,
                    ratePerUnit  = detail.ratePerUnit,
                    totalAmount  = detail.totalAmount,
                    unit         = detail.unit,
                    status       = 1,
                    createdAt    = now
                )
                contractEntryDao.insert(entity)
            }

            // ✅ Sync to API
            try { apiService.addContractEntries(req) }
            catch (e: Exception) { /* silent */ }

            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to save entries")
        }
    }

    suspend fun updateEntry(entry: ContractEntryEntity): Resource<Unit> {
        return try {
            contractEntryDao.update(entry)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to update")
        }
    }

    suspend fun deleteEntry(entryId: String): Resource<Unit> {
        return try {
            contractEntryDao.deleteEntry(entryId)
            Resource.Success(Unit)
        } catch (e: Exception) {
            Resource.Error(e.localizedMessage ?: "Failed to delete")
        }
    }
}