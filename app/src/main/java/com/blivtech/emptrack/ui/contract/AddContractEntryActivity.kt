package com.blivtech.emptrack.ui.contract

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ContractProductEntity
import com.blivtech.emptrack.data.local.entity.ShiftEntity
import com.blivtech.emptrack.data.model.ContractEntryRow
import com.blivtech.emptrack.databinding.ActivityAddContractEntryBinding
import com.blivtech.emptrack.databinding.ItemContractEntryRowBinding
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.Color

@AndroidEntryPoint
class AddContractEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddContractEntryBinding
    private val viewModel: ContractWageViewModel by viewModels()

    private val btCode      by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyName by lazy { intent.getStringExtra("companyName") ?: "" }
    private val isEdit      by lazy { intent.getBooleanExtra("isEdit", false) }
    private val editDate    by lazy { intent.getStringExtra("editDate") ?: "" }
    private val editShiftCode by lazy { intent.getStringExtra("editShiftCode") ?: "" }

    private var products = listOf<ContractProductEntity>()
    private var shifts   = listOf<ShiftEntity>()

    private val dateFmt    = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddContractEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupClickListeners()
        observeData()
    }

    private fun setupUI() {
        // ✅ Default date = today
        binding.tvDate.text = displayFmt.format(Date())
        binding.tvDateSub.text = "Today"

        // ✅ Add first empty row
        viewModel.addRow()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Date change
        binding.layoutDate.setOnClickListener { showDatePicker() }

        // ✅ Shift change
        binding.layoutShift.setOnClickListener { showShiftPicker() }

        // ✅ Add another row
        binding.btnAddRow.setOnClickListener { viewModel.addRow() }

        // ✅ Save
        binding.btnSave.setOnClickListener {
            viewModel.saveEntries(btCode, companyCode)
        }
    }

    private fun observeData() {
        // ✅ Load products
        viewModel.getProducts(btCode, companyCode).observe(this) { list ->
            products = list
        }

        // ✅ Load + auto detect shift
        viewModel.getShifts(companyCode).observe(this) { list ->
            shifts = list
            if (shifts.isNotEmpty() && viewModel.selectedShiftCode.isEmpty()) {
                viewModel.autoDetectShift(list)
                updateShiftUI()
            }
        }

        // ✅ Observe rows
        viewModel.entryRows.observe(this) { rows ->
            renderRows(rows)
            updateTotal(rows)
        }

        // ✅ Observe save state
        viewModel.saveState.observe(this) { resource ->
            resource ?: return@observe
            when (resource) {
                is Resource.Loading -> {
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is Resource.Success -> {
                    Snackbar.make(binding.root, "Entry saved ✅", Snackbar.LENGTH_SHORT).show()
                    viewModel.resetSaveState()
                    finish()
                }
                is Resource.Error -> {
                    binding.btnSave.isEnabled = true
                    updateSaveButton()
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetSaveState()
                }
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Render entry rows dynamically
    // ─────────────────────────────────
    private fun renderRows(rows: List<ContractEntryRow>) {
        binding.layoutRows.removeAllViews()

        rows.forEachIndexed { index, row ->
            val rowView = ItemContractEntryRowBinding.inflate(
                LayoutInflater.from(this), binding.layoutRows, false
            )

            val badgeNum = index + 1
            rowView.tvRowNum.text = badgeNum.toString()

            if (row.productId.isEmpty()) {
                // ✅ Empty row
                rowView.tvProductLabel.text = "Tap to select"
                rowView.tvProductSub.text = ""
                rowView.tvProductLabel.setTextColor(Color.parseColor("#BDBDBD"))
            } else {
                // ✅ Filled row
                rowView.tvProductLabel.text = row.productName
                rowView.tvProductSub.text = "${row.workName} · ₹${row.ratePerUnit}/${row.unit}"
                rowView.tvProductLabel.setTextColor(Color.parseColor("#212121"))
            }

            // ✅ Quantity
            rowView.tvQtyVal.text = if (row.quantityDone > 0)
                String.format("%.1f", row.quantityDone) else "0"
            rowView.tvQtyUnit.text = row.unit.ifEmpty { "—" }

            // ✅ Amount
            rowView.tvAmount.text = if (row.totalAmount > 0)
                "₹${String.format("%.0f", row.totalAmount)}" else "₹0"

            // ✅ Product picker tap
            rowView.layoutProductPicker.setOnClickListener {
                showProductPicker(index, row)
            }

            // ✅ Quantity tap
            rowView.layoutQty.setOnClickListener {
                if (row.productId.isEmpty()) {
                    Snackbar.make(binding.root, "Select a product first", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                showQtyInput(index, row)
            }

            // ✅ Delete row
            rowView.btnDelete.setOnClickListener {
                if (rows.size > 1) viewModel.removeRow(index)
                else Snackbar.make(binding.root, "At least one row required", Snackbar.LENGTH_SHORT).show()
            }

            binding.layoutRows.addView(rowView.root)
        }
    }

    // ─────────────────────────────────
    // ✅ Product picker bottom sheet
    // ─────────────────────────────────
    private fun showProductPicker(rowIndex: Int, currentRow: ContractEntryRow) {
        if (products.isEmpty()) {
            Snackbar.make(binding.root, "No products configured. Add product rates first!", Snackbar.LENGTH_LONG).show()
            return
        }

        val sheet = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this).inflate(
            R.layout.sheet_contract_product_picker, null
        )
        sheet.setContentView(sheetView)

        val container = sheetView.findViewById<android.widget.LinearLayout>(
            R.id.layoutContractProducts
        )

        products.forEach { product ->
            val item = LayoutInflater.from(this).inflate(
                R.layout.item_contract_product_sheet, container, false
            )
            val dot = item.findViewById<View>(R.id.viewDot)
            try {
                dot.setBackgroundColor(Color.parseColor(product.colorTag))
            } catch (e: Exception) {
                dot.setBackgroundColor(Color.parseColor("#1565C0"))
            }
            item.findViewById<TextView>(R.id.tvProductName).text = product.productName
            item.findViewById<TextView>(R.id.tvWorkName).text =
                "${product.workName} · ₹${product.ratePerUnit}/${product.unit}"

            if (product.productId == currentRow.productId) {
                item.setBackgroundColor(Color.parseColor("#F0F7FF"))
                item.findViewById<View>(R.id.ivCheck).visibility = View.VISIBLE
            }

            item.setOnClickListener {
                val updatedRow = currentRow.copy(
                    productId   = product.productId,
                    productName = product.productName,
                    workName    = product.workName,
                    ratePerUnit = product.ratePerUnit,
                    unit        = product.unit,
                    colorTag    = product.colorTag,
                    totalAmount = currentRow.quantityDone * product.ratePerUnit
                )
                viewModel.updateRow(rowIndex, updatedRow)
                sheet.dismiss()
            }

            container.addView(item)
        }

        sheet.show()
    }

    // ─────────────────────────────────
    // ✅ Qty input bottom sheet
    // ─────────────────────────────────
    private fun showQtyInput(rowIndex: Int, currentRow: ContractEntryRow) {
        val sheet = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this).inflate(
            R.layout.sheet_contract_qty_input, null
        )
        sheet.setContentView(sheetView)

        val tvProduct = sheetView.findViewById<TextView>(R.id.tvQtyProduct)
        val etQty     = sheetView.findViewById<android.widget.EditText>(R.id.etQty)
        val tvUnit    = sheetView.findViewById<TextView>(R.id.tvQtyUnit)
        val tvPreview = sheetView.findViewById<TextView>(R.id.tvQtyPreview)
        val btnSave   = sheetView.findViewById<android.widget.Button>(R.id.btnQtySave)

        tvProduct.text = "${currentRow.productName} · ${currentRow.workName}"
        tvUnit.text    = currentRow.unit
        if (currentRow.quantityDone > 0) {
            etQty.setText(String.format("%.1f", currentRow.quantityDone))
        }

        fun updatePreview() {
            val qty = etQty.text.toString().toDoubleOrNull() ?: 0.0
            val amt = qty * currentRow.ratePerUnit
            tvPreview.text = "${String.format("%.1f", qty)} ${currentRow.unit} × ₹${currentRow.ratePerUnit} = ₹${String.format("%.0f", amt)}"
        }
        updatePreview()

        etQty.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { updatePreview() }
            override fun beforeTextChanged(s: CharSequence?, st: Int, c: Int, a: Int) {}
            override fun onTextChanged(s: CharSequence?, st: Int, b: Int, c: Int) {}
        })

        btnSave.setOnClickListener {
            val qty = etQty.text.toString().toDoubleOrNull()
            if (qty == null || qty <= 0) {
                Snackbar.make(binding.root, "Enter valid quantity", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val updatedRow = currentRow.copy(
                quantityDone = qty,
                totalAmount  = qty * currentRow.ratePerUnit
            )
            viewModel.updateRow(rowIndex, updatedRow)
            sheet.dismiss()
        }

        sheet.show()
    }

    // ─────────────────────────────────
    // ✅ Shift picker
    // ─────────────────────────────────
    private fun showShiftPicker() {
        if (shifts.isEmpty()) {
            Snackbar.make(binding.root, "No shifts found", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sheet = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this).inflate(
            R.layout.sheet_contract_shift_picker, null
        )
        sheet.setContentView(sheetView)

        val container = sheetView.findViewById<android.widget.LinearLayout>(
            R.id.layoutShifts
        )

        shifts.forEach { shift ->
            val item = LayoutInflater.from(this).inflate(
                R.layout.item_contract_shift, container, false
            )
            item.findViewById<TextView>(R.id.tvShiftName).text = shift.shiftName
            item.findViewById<TextView>(R.id.tvShiftTime).text =
                "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"

            if (shift.shiftCode == viewModel.selectedShiftCode) {
                item.setBackgroundColor(Color.parseColor("#F0F7FF"))
            //    item.findViewById<View>(R.id.ivShiftCheck).visibility = View.VISIBLE
            }

            item.setOnClickListener {
                viewModel.selectedShiftCode = shift.shiftCode
                viewModel.selectedShiftName = shift.shiftName
                viewModel.selectedShiftTime = "${shift.startTime.take(5)} – ${shift.endTime.take(5)}"
                updateShiftUI()
                sheet.dismiss()
            }

            container.addView(item)
        }

        sheet.show()
    }

    // ─────────────────────────────────
    // ✅ Date picker
    // ─────────────────────────────────
    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                cal.set(year, month, day)
                viewModel.selectedDate = dateFmt.format(cal.time)
                viewModel.selectedDisplayDate = displayFmt.format(cal.time)
                binding.tvDate.text = viewModel.selectedDisplayDate
                binding.tvDateSub.text = if (viewModel.isToday) "Today" else ""
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = System.currentTimeMillis()  // ✅ No future dates
        }.show()
    }

    private fun updateShiftUI() {
        binding.tvShiftName.text = viewModel.selectedShiftName
        binding.tvShiftTime.text = viewModel.selectedShiftTime
    }

    private fun updateTotal(rows: List<ContractEntryRow>) {
        val total = rows.sumOf { it.totalAmount }
        binding.tvTotal.text = "₹${String.format("%.0f", total)}"
        binding.tvTotalEntries.text = "${rows.filter { it.productId.isNotEmpty() }.size} entries"
        updateSaveButton()
    }

    private fun updateSaveButton() {
        val total = viewModel.getTotalAmount()
        val hasRows = viewModel.getFilledRows().isNotEmpty()
        binding.btnSave.isEnabled = hasRows
        binding.btnSave.text = if (hasRows)
            "Save Entry — ₹${String.format("%.0f", total)}"
        else "Save Entry"
    }
}