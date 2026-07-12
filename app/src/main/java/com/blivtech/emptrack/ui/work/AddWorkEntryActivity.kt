package com.blivtech.emptrack.ui.work

import android.app.DatePickerDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.*
import com.blivtech.emptrack.databinding.ActivityAddWorkEntryBinding
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class AddWorkEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddWorkEntryBinding
    private val viewModel: AddWorkEntryViewModel by viewModels()

    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val companyId by lazy { intent.getLongExtra("companyId", -1L) }

    private val dateFmt = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val displayFmt = SimpleDateFormat("d MMM yyyy", Locale.getDefault())

    // ─────────────────────────────────────
    // ✅ Modern Activity Result API — replaces the broken
    //    startActivity() + onActivityResult() pattern.
    //    THIS is the "proper order" fix: the result now
    //    actually comes back into this screen.
    // ─────────────────────────────────────
    private val employeePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data ?: return@registerForActivityResult
            val empCode = data.getStringExtra("empCode") ?: ""
            val empName = data.getStringExtra("empName") ?: ""
            setEmployee(empCode, empName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddWorkEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ✅ Set today as default date
        val today = dateFmt.format(Date())
        viewModel.selectedDate = today
        binding.tvDate.text = displayFmt.format(Date())
        binding.tvDateSub.text = "Today"

        setupClickListeners()
        observeViewModel()

        // ✅ Load products from API
        viewModel.loadProducts(btCode, companyCode)
    }

    // ─────────────────────────────────────
    // ✅ Setup click listeners
    // ─────────────────────────────────────
    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Date picker
        binding.layoutDate.setOnClickListener { showDatePicker() }

        // ✅ Employee picker — tap empty banner
        binding.layoutNoEmployee.setOnClickListener { openEmployeePicker() }

        // ✅ Change employee button
        binding.btnChangeEmployee.setOnClickListener { openEmployeePicker() }

        // ✅ Add another product row
        binding.btnAddRow.setOnClickListener { viewModel.addRow() }

        // ✅ Save button
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                viewModel.saveEntries(btCode, companyCode)
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Observe ViewModel
    // ─────────────────────────────────────
    private fun observeViewModel() {

        // ✅ Products loaded — rebuild rows so dropdowns refresh
        viewModel.products.observe(this) {
            val rows = viewModel.rows.value ?: mutableListOf()
            buildEntryRows(rows)
        }

        // ✅ Rows changed
        viewModel.rows.observe(this) { rows ->
            buildEntryRows(rows)
            updateTotalBar(rows)
            updateSaveButton()
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled = false
                    binding.btnSave.text = "Saving..."
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Work Entry"
                    Snackbar.make(binding.root, resource.data, Snackbar.LENGTH_SHORT).show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled = true
                    binding.btnSave.text = "Save Work Entry"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                }
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Build entry rows dynamically
    // ─────────────────────────────────────
    private fun buildEntryRows(rows: List<WorkEntryRow>) {
        binding.layoutEntries.removeAllViews()

        rows.forEachIndexed { index, row ->
            val cardView = layoutInflater.inflate(
                R.layout.item_work_entry_row,
                binding.layoutEntries,
                false
            )

            // ─── Row number badge ─────────────
            cardView.findViewById<TextView>(R.id.tvRowBadge).text = "${index + 1}"

            // ─── Row label ────────────────────
            cardView.findViewById<TextView>(R.id.tvRowLabel).text = if (row.isValid)
                "${row.productName} · ${row.workTypeName}"
            else
                "Select product & work type"

            // ─── Product picker ───────────────
            val layoutProduct = cardView.findViewById<LinearLayout>(R.id.layoutProduct)
            val tvProductName = cardView.findViewById<TextView>(R.id.tvProductName)
            tvProductName.text = row.productName.ifEmpty { "Select product" }
            tvProductName.setTextColor(
                if (row.productId.isEmpty()) Color.parseColor("#BDBDBD")
                else Color.parseColor("#212121")
            )
            layoutProduct.setOnClickListener { showProductPicker(index, row) }

            // ─── Work type picker ─────────────
            val layoutWorkType = cardView.findViewById<LinearLayout>(R.id.layoutWorkType)
            val tvWorkTypeName = cardView.findViewById<TextView>(R.id.tvWorkTypeName)
            val tvWorkTypeRate = cardView.findViewById<TextView>(R.id.tvWorkTypeRate)
            tvWorkTypeName.text = row.workTypeName.ifEmpty { "Select work type" }
            tvWorkTypeName.setTextColor(
                if (row.workTypeId.isEmpty()) Color.parseColor("#BDBDBD")
                else Color.parseColor("#212121")
            )
            tvWorkTypeRate.text = if (row.ratePerPiece > 0)
                "₹${String.format("%.2f", row.ratePerPiece)}/pc"
            else ""
            tvWorkTypeRate.visibility = if (row.ratePerPiece > 0) View.VISIBLE else View.GONE

            layoutWorkType.setOnClickListener {
                if (row.productId.isEmpty()) {
                    Snackbar.make(binding.root, "Select product first", Snackbar.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                showWorkTypePicker(index, row)
            }

            // ─── Pieces input ─────────────────
            val etPieces = cardView.findViewById<EditText>(R.id.etPieces)
            etPieces.setText(if (row.piecesDone > 0) row.piecesDone.toInt().toString() else "")
            etPieces.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val pieces = s.toString().toDoubleOrNull() ?: 0.0
                    val updatedRow = row.copy(
                        piecesDone = pieces,
                        totalAmount = pieces * row.ratePerPiece
                    )
                    viewModel.updateRow(index, updatedRow)
                }
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })

            // ─── Amount display ───────────────
            val tvAmount = cardView.findViewById<TextView>(R.id.tvAmount)
            val layoutAmount = cardView.findViewById<LinearLayout>(R.id.layoutAmount)
            if (row.totalAmount > 0) {
                tvAmount.text = "₹${String.format("%.0f", row.totalAmount)}"
                layoutAmount.visibility = View.VISIBLE
            } else {
                layoutAmount.visibility = View.INVISIBLE
            }

            // ─── Card border ──────────────────
            val card = cardView.findViewById<MaterialCardView>(R.id.cardEntry)
            card.strokeWidth = if (row.isValid) 2 else 0
            card.strokeColor = Color.parseColor("#1565C0")

            // ─── Delete button ────────────────
            val ivDelete = cardView.findViewById<ImageView>(R.id.ivDeleteRow)
            ivDelete.visibility = if (rows.size > 1) View.VISIBLE else View.INVISIBLE
            ivDelete.setOnClickListener { viewModel.removeRow(index) }

            binding.layoutEntries.addView(cardView)
        }
    }

    // ─────────────────────────────────────
    // ✅ Product picker bottom sheet
    // ─────────────────────────────────────
    private fun showProductPicker(rowIndex: Int, row: WorkEntryRow) {
        val products = viewModel.products.value ?: return
        if (products.isEmpty()) {
            Snackbar.make(binding.root, "No products found", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sheetView = layoutInflater.inflate(R.layout.sheet_product_picker, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)

        val container = sheetView.findViewById<LinearLayout>(R.id.layoutProducts)

        products.forEach { product ->
            val item = layoutInflater.inflate(R.layout.item_product_sheet, container, false)
            item.findViewById<TextView>(R.id.tvProductName).text = product.productName
            item.findViewById<TextView>(R.id.tvWorkTypeCount).text =
                "${product.workTypes.size} work types"

            if (product.productId == row.productId) {
                item.setBackgroundColor(Color.parseColor("#F0F7FF"))
            }

            item.setOnClickListener {
                val updatedRow = row.copy(
                    productId = product.productId,
                    productName = product.productName,
                    // ✅ Reset work type when product changes
                    workTypeId = "",
                    workTypeName = "",
                    ratePerPiece = 0.0,
                    totalAmount = 0.0
                )
                viewModel.updateRow(rowIndex, updatedRow)
                dialog.dismiss()
            }

            container.addView(item)
        }

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Work type picker bottom sheet
    // ─────────────────────────────────────
    private fun showWorkTypePicker(rowIndex: Int, row: WorkEntryRow) {
        val workTypes = viewModel.getWorkTypesForProduct(row.productId)
        if (workTypes.isEmpty()) {
            Snackbar.make(binding.root, "No work types for this product", Snackbar.LENGTH_SHORT).show()
            return
        }

        val sheetView = layoutInflater.inflate(R.layout.sheet_worktype_picker, null)
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)

        sheetView.findViewById<TextView>(R.id.tvSheetSub).text =
            "${row.productName} — tap to select"

        val container = sheetView.findViewById<LinearLayout>(R.id.layoutWorkTypes)

        workTypes.forEach { wt ->
            val item = layoutInflater.inflate(R.layout.item_worktype_sheet, container, false)
            val colorDot = item.findViewById<View>(R.id.viewDot)
            colorDot.setBackgroundColor(
                try { Color.parseColor(wt.colorTag) }
                catch (e: Exception) { Color.parseColor("#1565C0") }
            )
            item.findViewById<TextView>(R.id.tvWorkTypeName).text = wt.workTypeName
            item.findViewById<TextView>(R.id.tvRate).text =
                "₹${String.format("%.2f", wt.ratePerPiece)}/${wt.unit}"

            if (wt.workTypeId == row.workTypeId) {
                item.setBackgroundColor(Color.parseColor("#F0F7FF"))
                item.findViewById<ImageView>(R.id.ivCheck).visibility = View.VISIBLE
            }

            item.setOnClickListener {
                val updatedRow = row.copy(
                    workTypeId = wt.workTypeId,
                    workTypeName = wt.workTypeName,
                    ratePerPiece = wt.ratePerPiece,
                    totalAmount = row.piecesDone * wt.ratePerPiece
                )
                viewModel.updateRow(rowIndex, updatedRow)
                dialog.dismiss()
            }

            container.addView(item)
        }

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Update total bar
    // ─────────────────────────────────────
    private fun updateTotalBar(rows: List<WorkEntryRow>) {
        val total = viewModel.getTotalAmount()
        val count = viewModel.getValidRowCount()
        val pieces = rows.filter { it.isValid }.sumOf { it.piecesDone }

        if (total > 0) {
            binding.layoutTotalBar.visibility = View.VISIBLE
            binding.tvTotalAmount.text = "₹${String.format("%.0f", total)}"
            binding.tvTotalInfo.text = "$count entries · ${pieces.toInt()} pieces"
        } else {
            binding.layoutTotalBar.visibility = View.GONE
        }
    }

    // ─────────────────────────────────────
    // ✅ Update save button
    // ─────────────────────────────────────
    private fun updateSaveButton() {
        val hasEmployee = viewModel.selectedEmpCode.isNotEmpty()
        val hasEntries = viewModel.getValidRowCount() > 0
        val isReady = hasEmployee && hasEntries

        binding.btnSave.isEnabled = isReady
        binding.btnSave.alpha = if (isReady) 1.0f else 0.5f

        val total = viewModel.getTotalAmount()
        binding.btnSave.text = if (isReady)
            "Save Work Entry — ₹${String.format("%.0f", total)}"
        else
            "Fill entries to save"
    }

    // ─────────────────────────────────────
    // ✅ Date picker
    // ─────────────────────────────────────
    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                val cal2 = Calendar.getInstance()
                cal2.set(year, month, day)
                viewModel.selectedDate = dateFmt.format(cal2.time)
                binding.tvDate.text = displayFmt.format(cal2.time)

                val today = dateFmt.format(Date())
                binding.tvDateSub.text = if (viewModel.selectedDate == today) "Today" else ""
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    // ─────────────────────────────────────
    // ✅ Open employee picker (via result launcher)
    // ─────────────────────────────────────
    private fun openEmployeePicker() {
        employeePickerLauncher.launch(
            Intent(this, SelectEmployeeActivity::class.java).apply {
                putExtra("btCode", btCode)
                putExtra("companyId", companyId)
                putExtra("companyCode", companyCode)
            }
        )
    }

    // ─────────────────────────────────────
    // ✅ Apply selected employee to UI
    // ─────────────────────────────────────
    private fun setEmployee(empCode: String, empName: String) {
        viewModel.selectedEmpCode = empCode
        viewModel.selectedEmpName = empName

        binding.layoutNoEmployee.visibility = View.GONE
        binding.layoutEmployeeBanner.visibility = View.VISIBLE
        binding.tvEmpInitials.text = empName.split(" ")
            .take(2)
            .filter { it.isNotEmpty() }
            .joinToString("") { it.first().uppercase() }
        binding.tvEmpName.text = empName
        binding.tvEmpCode.text = empCode

        updateSaveButton()
    }

    // ─────────────────────────────────────
    // ✅ Validate
    // ─────────────────────────────────────
    private fun validateInputs(): Boolean {
        if (viewModel.selectedEmpCode.isEmpty()) {
            Snackbar.make(binding.root, "Please select an employee", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (viewModel.selectedDate.isEmpty()) {
            Snackbar.make(binding.root, "Please select a date", Snackbar.LENGTH_SHORT).show()
            return false
        }
        if (viewModel.getValidRowCount() == 0) {
            Snackbar.make(binding.root, "Add at least one work entry", Snackbar.LENGTH_SHORT).show()
            return false
        }
        return true
    }
}