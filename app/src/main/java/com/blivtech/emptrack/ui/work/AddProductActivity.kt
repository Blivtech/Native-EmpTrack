package com.blivtech.emptrack.ui.work

import android.app.DatePickerDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.databinding.ActivityAddProductBinding
import com.blivtech.emptrack.utils.Resource
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddProductActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddProductBinding
    private val viewModel: AddProductViewModel by viewModels()

    private val btCode by lazy { intent.getStringExtra("btCode") ?: "" }
    private val companyCode by lazy { intent.getStringExtra("companyCode") ?: "" }
    private val isEditMode by lazy { intent.getBooleanExtra("isEdit", false) }
    private val productId by lazy { intent.getStringExtra("productId") ?: "" }

    // ✅ Color tags available
    private val colors = listOf(
        "#1565C0", "#EF9F27", "#639922",
        "#7F77DD", "#E24B4A", "#633806"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddProductBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvTitle.text = if (isEditMode) "Edit Product" else "Add Product"

        setupClickListeners()
        observeViewModel()

        // ✅ Pre-fill if edit mode
        if (isEditMode) {
            val productName = intent.getStringExtra("productName") ?: ""
            val description = intent.getStringExtra("description") ?: ""
            binding.etProductName.setText(productName)
            binding.etDescription.setText(description)
        }
    }

    // ─────────────────────────────────────
    // ✅ Setup click listeners
    // ─────────────────────────────────────
    private fun setupClickListeners() {
        binding.ivBack.setOnClickListener { finish() }

        // ✅ Add work type button
        binding.btnAddWorkType.setOnClickListener {
            showAddWorkTypeSheet()
        }

        // ✅ Save button
        binding.btnSave.setOnClickListener {
            if (validateInputs()) {
                viewModel.saveProduct(
                    btCode       = btCode,
                    companyCode  = companyCode,
                    productName  = binding.etProductName.text.toString().trim(),
                    description  = binding.etDescription.text.toString().trim()
                        .ifEmpty { null },
                    isEdit       = isEditMode,
                    productId    = productId
                )
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Observe ViewModel
    // ─────────────────────────────────────
    private fun observeViewModel() {

        // ✅ Work types list
        viewModel.workTypes.observe(this) { list ->
            buildWorkTypeCards(list)
            updateSaveButton(list)
        }

        // ✅ Save state
        viewModel.saveState.observe(this) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnSave.isEnabled      = false
                    binding.btnSave.text           = "Saving..."
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled      = true
                    binding.btnSave.text           = "Save Product"
                    Snackbar.make(binding.root, resource.data, Snackbar.LENGTH_SHORT)
                        .show()
                    finish()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnSave.isEnabled      = true
                    binding.btnSave.text           = "Save Product"
                    Snackbar.make(binding.root, resource.message, Snackbar.LENGTH_LONG)
                        .show()
                }
            }
        }
    }

    // ─────────────────────────────────────
    // ✅ Build work type cards dynamically
    // ─────────────────────────────────────
    private fun buildWorkTypeCards(list: List<WorkTypeItem>) {
        binding.layoutWorkTypes.removeAllViews()

        list.forEachIndexed { index, item ->
            val card = layoutInflater.inflate(
                R.layout.item_work_type_card,
                binding.layoutWorkTypes,
                false
            )

            // ✅ Color dot
            val colorDot = card.findViewById<View>(R.id.viewColorDot)
            colorDot.setBackgroundColor(Color.parseColor(item.colorTag))

            // ✅ Info
            card.findViewById<TextView>(R.id.tvWorkTypeName).text = item.name
            card.findViewById<TextView>(R.id.tvWorkTypeRate).text =
                "₹${String.format("%.2f", item.ratePerPiece)} / ${item.unit}"

            // ✅ Edit button
            card.findViewById<ImageView>(R.id.ivEdit).setOnClickListener {
                showAddWorkTypeSheet(index, item)
            }

            // ✅ Delete button
            card.findViewById<ImageView>(R.id.ivDelete).setOnClickListener {
                viewModel.removeWorkType(index)
            }

            binding.layoutWorkTypes.addView(card)
        }
    }

    // ─────────────────────────────────────
    // ✅ Show add/edit work type bottom sheet
    // ─────────────────────────────────────
    private fun showAddWorkTypeSheet(
        editIndex: Int = -1,
        editItem: WorkTypeItem? = null
    ) {
        val sheetView = layoutInflater.inflate(
            R.layout.sheet_add_work_type,
            null
        )
        val dialog = BottomSheetDialog(this)
        dialog.setContentView(sheetView)

        val etName      = sheetView.findViewById<TextInputEditText>(R.id.etWorkTypeName)
        val etRate      = sheetView.findViewById<TextInputEditText>(R.id.etRate)
        val etUnit      = sheetView.findViewById<TextInputEditText>(R.id.etUnit)
        val colorLayout = sheetView.findViewById<LinearLayout>(R.id.layoutColors)
        val tvPreview   = sheetView.findViewById<TextView>(R.id.tvPreview)
        val viewPreviewDot = sheetView.findViewById<View>(R.id.viewPreviewDot)
        val btnSave     = sheetView.findViewById<Button>(R.id.btnSaveWorkType)
        val tvSheetTitle = sheetView.findViewById<TextView>(R.id.tvSheetTitle)

        var selectedColor = editItem?.colorTag ?: colors[0]

        // ✅ Pre-fill if edit
        if (editItem != null) {
            tvSheetTitle.text = "Edit Work Type"
            etName.setText(editItem.name)
            etRate.setText(editItem.ratePerPiece.toString())
            etUnit.setText(editItem.unit)
            selectedColor = editItem.colorTag
        } else {
            tvSheetTitle.text = "Add Work Type"
            etUnit.setText("pieces")
        }

        // ✅ Build color picker
        colors.forEach { color ->
            val dot = layoutInflater.inflate(
                R.layout.item_color_dot, colorLayout, false
            )
            val view = dot.findViewById<View>(R.id.viewDot)
            view.setBackgroundColor(Color.parseColor(color))

            if (color == selectedColor) {
                view.scaleX = 1.3f
                view.scaleY = 1.3f
            }

            view.setOnClickListener {
                selectedColor = color
                // ✅ Reset all dots
                for (i in 0 until colorLayout.childCount) {
                    val child = colorLayout.getChildAt(i)
                        .findViewById<View>(R.id.viewDot)
                    child.scaleX = 1.0f
                    child.scaleY = 1.0f
                }
                view.scaleX = 1.3f
                view.scaleY = 1.3f
                // ✅ Update preview
                viewPreviewDot.setBackgroundColor(Color.parseColor(color))
            }

            colorLayout.addView(dot)
        }

        // ✅ Update preview on name type
        etName.setOnFocusChangeListener { _, _ ->
            val name = etName.text.toString().trim()
            val rate = etRate.text.toString().toDoubleOrNull() ?: 0.0
            val unit = etUnit.text.toString().trim().ifEmpty { "pieces" }
            tvPreview.text = "$name — ₹${String.format("%.2f", rate)}/$unit"
            viewPreviewDot.setBackgroundColor(Color.parseColor(selectedColor))
        }

        // ✅ Save button
        btnSave.setOnClickListener {
            val name = etName.text.toString().trim()
            val rate = etRate.text.toString().toDoubleOrNull() ?: 0.0
            val unit = etUnit.text.toString().trim().ifEmpty { "pieces" }

            if (name.isEmpty()) {
                etName.error = "Work type name required"
                return@setOnClickListener
            }
            if (rate <= 0.0) {
                etRate.error = "Rate must be > 0"
                return@setOnClickListener
            }

            val item = WorkTypeItem(
                id           = editIndex,
                name         = name,
                ratePerPiece = rate,
                unit         = unit,
                colorTag     = selectedColor
            )

            if (editIndex >= 0)
                viewModel.updateWorkType(editIndex, item)
            else
                viewModel.addWorkType(item)

            dialog.dismiss()
        }

        dialog.show()
    }

    // ─────────────────────────────────────
    // ✅ Validate inputs
    // ─────────────────────────────────────
    private fun validateInputs(): Boolean {
        val name = binding.etProductName.text.toString().trim()
        if (name.isEmpty()) {
            binding.tilProductName.error = "Product name required"
            return false
        }
        binding.tilProductName.error = null

        val workTypes = viewModel.workTypes.value
        if (workTypes.isNullOrEmpty()) {
            Snackbar.make(
                binding.root,
                "Add at least one work type",
                Snackbar.LENGTH_SHORT
            ).show()
            return false
        }
        return true
    }

    // ─────────────────────────────────────
    // ✅ Update save button state
    // ─────────────────────────────────────
    private fun updateSaveButton(list: List<WorkTypeItem>) {
        val hasProduct = binding.etProductName.text.toString().trim().isNotEmpty()
        val hasWorkTypes = list.isNotEmpty()
        binding.btnSave.alpha = if (hasProduct && hasWorkTypes) 1.0f else 0.5f
    }
}