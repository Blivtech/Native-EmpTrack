package com.blivtech.emptrack.ui.contract

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.local.entity.ContractProductEntity
import com.blivtech.emptrack.data.model.ContractProductRequest
import com.blivtech.emptrack.databinding.ActivityContractProductListBinding
import com.blivtech.emptrack.databinding.ItemContractProductBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import android.graphics.Color

@AndroidEntryPoint
class ContractProductListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityContractProductListBinding
    private val viewModel: ContractWageViewModel by viewModels()

    private val btCode      by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }

    private var units = listOf<String>()
    private var products = listOf<ContractProductEntity>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityContractProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupClickListeners()
        observeData()
    }

    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }
        binding.btnAddProduct.setOnClickListener { showAddProductSheet(null) }
    }

    private fun observeData() {
        // ✅ Units from Room
        viewModel.getUnits(btCode).observe(this) { unitList ->
            units = unitList.map { it.unitName }
        }

        // ✅ Products from Room
        viewModel.getProducts(btCode, companyCode).observe(this) { list ->
            products = list
            renderProducts(list)
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { resource ->
            resource ?: return@observe
            when (resource) {
                is com.blivtech.emptrack.utils.Resource.Success -> {
                    Snackbar.make(binding.root, "Product saved ✅", Snackbar.LENGTH_SHORT).show()
                    viewModel.resetSaveState()
                }
                is com.blivtech.emptrack.utils.Resource.Error -> {
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG).show()
                    viewModel.resetSaveState()
                }
                else -> {}
            }
        }
    }

    // ─────────────────────────────────
    // ✅ Render product list
    // ─────────────────────────────────
    private fun renderProducts(list: List<ContractProductEntity>) {
        binding.layoutProducts.removeAllViews()

        if (list.isEmpty()) {
            binding.layoutEmpty.visibility = View.VISIBLE
            return
        }
        binding.layoutEmpty.visibility = View.GONE

        list.forEach { product ->
            val card = ItemContractProductBinding.inflate(
                LayoutInflater.from(this), binding.layoutProducts, false
            )
            try {
                card.viewDot.setBackgroundColor(Color.parseColor(product.colorTag))
            } catch (e: Exception) {}
            card.tvProductName.text = product.productName
            card.tvWorkName.text    = product.workName
            card.tvRate.text        = "₹${product.ratePerUnit}/${product.unit}"

            // ✅ Edit
            card.btnEdit.setOnClickListener { showAddProductSheet(product) }

            // ✅ Delete
            card.btnDelete.setOnClickListener {
                AlertDialog.Builder(this)
                    .setTitle("Delete product")
                    .setMessage("Delete ${product.productName}?")
                    .setPositiveButton("Delete") { _, _ ->
                        viewModel.deleteProduct(product.productId)
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            }

            binding.layoutProducts.addView(card.root)
        }
    }

    // ─────────────────────────────────
    // ✅ Add/Edit product bottom sheet
    // ─────────────────────────────────
    private fun showAddProductSheet(existing: ContractProductEntity?) {
        val sheet = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this).inflate(
            R.layout.sheet_add_contract_product, null
        )
        sheet.setContentView(sheetView)

        val tvTitle    = sheetView.findViewById<TextView>(R.id.tvSheetTitle)
        val etProduct  = sheetView.findViewById<android.widget.EditText>(R.id.etProductName)
        val etWork     = sheetView.findViewById<android.widget.EditText>(R.id.etWorkName)
        val etRate     = sheetView.findViewById<android.widget.EditText>(R.id.etRate)
        val unitChips  = sheetView.findViewById<android.widget.LinearLayout>(R.id.layoutUnitChips)
        val etCustomUnit = sheetView.findViewById<android.widget.EditText>(R.id.etCustomUnit)
        val btnAddUnit = sheetView.findViewById<android.widget.Button>(R.id.btnAddUnit)
        val btnSave    = sheetView.findViewById<android.widget.Button>(R.id.btnSaveProduct)

        tvTitle.text = if (existing != null) "Edit Product Rate" else "Add Product Rate"

        // ✅ Pre-fill if editing
        existing?.let {
            etProduct.setText(it.productName)
            etWork.setText(it.workName)
            etRate.setText(it.ratePerUnit.toString())
        }

        var selectedUnit = existing?.unit ?: ""

        // ✅ Build unit chips dynamically
        fun buildUnitChips() {
            unitChips.removeAllViews()
            units.forEach { unitName ->
                val chip = android.widget.TextView(this).apply {
                    text = unitName
                    setPadding(24, 12, 24, 12)
                    textSize = 11f
                    setTypeface(typeface, android.graphics.Typeface.BOLD)
                    val isSelected = unitName == selectedUnit
                    setTextColor(if (isSelected) Color.parseColor("#0C447C") else Color.parseColor("#9E9E9E"))
                    setBackgroundResource(
                        if (isSelected) R.drawable.bg_unit_chip_selected
                        else R.drawable.bg_unit_chip
                    )
                    val lp = android.widget.LinearLayout.LayoutParams(
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT,
                        android.widget.LinearLayout.LayoutParams.WRAP_CONTENT
                    )
                    lp.marginEnd = 8
                    layoutParams = lp
                    setOnClickListener {
                        selectedUnit = unitName
                        buildUnitChips()
                    }
                }
                unitChips.addView(chip)
            }
        }
        buildUnitChips()

        // ✅ Add custom unit
        btnAddUnit.setOnClickListener {
            val newUnit = etCustomUnit.text.toString().trim()
            if (newUnit.isEmpty()) return@setOnClickListener
            viewModel.addUnit(btCode, newUnit)
            selectedUnit = newUnit
            etCustomUnit.text?.clear()
            Snackbar.make(binding.root, "Unit '$newUnit' added", Snackbar.LENGTH_SHORT).show()
        }

        // ✅ Save
        btnSave.setOnClickListener {
            val productName = etProduct.text.toString().trim()
            val workName    = etWork.text.toString().trim()
            val rate        = etRate.text.toString().toDoubleOrNull()

            if (productName.isEmpty()) { etProduct.error = "Required"; return@setOnClickListener }
            if (workName.isEmpty())    { etWork.error = "Required"; return@setOnClickListener }
            if (rate == null || rate <= 0) { etRate.error = "Enter valid rate"; return@setOnClickListener }
            if (selectedUnit.isEmpty()) {
                Snackbar.make(binding.root, "Select a unit", Snackbar.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (existing != null) {
                // ✅ Update
                viewModel.updateProduct(
                    existing.copy(
                        productName = productName,
                        workName    = workName,
                        ratePerUnit = rate,
                        unit        = selectedUnit
                    )
                )
            } else {
                // ✅ Add new
                viewModel.addProduct(
                    ContractProductRequest(
                        btCode      = btCode,
                        companyCode = companyCode,
                        productName = productName,
                        workName    = workName,
                        ratePerUnit = rate,
                        unit        = selectedUnit
                    )
                )
            }
            sheet.dismiss()
        }

        // ✅ Also observe units in sheet
        viewModel.getUnits(btCode).observe(this) { unitList ->
            units = unitList.map { it.unitName }
            buildUnitChips()
        }

        sheet.show()
    }

    private fun updateProduct(product: ContractProductEntity) {
        viewModel.updateProduct(product)
    }
}

