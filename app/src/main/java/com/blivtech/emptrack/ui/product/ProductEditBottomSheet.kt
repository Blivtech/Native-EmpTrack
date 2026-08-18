package com.blivtech.emptrack.ui.product

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.model.ProductRequestDto
import com.blivtech.emptrack.data.model.WorkLineRequestDto

import com.blivtech.emptrack.databinding.ItemWorkEditRowBinding
import com.blivtech.emptrack.databinding.SheetProductEditBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductEditBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetProductEditBinding? = null
    private val binding get() = _binding!!

    // Shares the Activity's ViewModel instance.
    private val viewModel: ProductViewModel by activityViewModels()

    private val iconKeys = listOf("shirt", "trouser", "dress", "tshirt", "blouse", "fabric")
    private var selectedIcon = "shirt"
    private var editingId: Long? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetProductEditBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        buildIconChips()
        prefill(viewModel.editTarget.value)

        binding.addWorkRow.setOnClickListener { addWorkRow("", "") }
        binding.close.setOnClickListener { dismiss() }
        binding.cancel.setOnClickListener { dismiss() }
        binding.save.setOnClickListener { onSave() }
        binding.delete.setOnClickListener { confirmDelete() }

        observeSaveState()
    }

    private fun prefill(target: ProductWithWorks?) {
        binding.worksContainer.removeAllViews()
        if (target == null) {
            editingId = null
            binding.title.text = "Add product"
            binding.subtitle.text = "Set the works and piece-rates"
            binding.delete.visibility = View.GONE
            binding.nameInput.setText("")
            binding.unitInput.setText("pieces")
            selectIcon("shirt")
            addWorkRow("Stitching", "8")
            addWorkRow("Ironing", "1.5")
        } else {
            editingId = target.product.id
            binding.title.text = "Edit product"
            binding.subtitle.text = target.product.productCode
            binding.delete.visibility = View.VISIBLE
            binding.nameInput.setText(target.product.name)
            binding.unitInput.setText(target.product.unit)
            selectIcon(target.product.icon ?: "shirt")
            if (target.works.isEmpty()) addWorkRow("", "")
            else target.works.forEach { addWorkRow(it.workName, fmtRate(it.rate)) }
        }
    }

    private fun buildIconChips() {
        binding.iconGroup.removeAllViews()
        iconKeys.forEach { key ->
            val chip = Chip(requireContext()).apply {
                text = key.replaceFirstChar { it.uppercase() }
                isCheckable = true
                isChecked = key == selectedIcon
                setOnClickListener { selectIcon(key) }
            }
            binding.iconGroup.addView(chip)
        }
    }

    private fun selectIcon(key: String) {
        selectedIcon = key
        for (i in 0 until binding.iconGroup.childCount) {
            val chip = binding.iconGroup.getChildAt(i) as? Chip ?: continue
            chip.isChecked = chip.text.toString().equals(key, ignoreCase = true)
        }
    }

    private fun addWorkRow(name: String, rate: String) {
        val row = ItemWorkEditRowBinding.inflate(layoutInflater, binding.worksContainer, false)
        row.workName.setText(name)
        row.rate.setText(rate)
        row.remove.setOnClickListener {
            if (binding.worksContainer.childCount > 1) binding.worksContainer.removeView(row.root)
            else toast("Keep at least one work")
        }
        binding.worksContainer.addView(row.root)
    }

    private fun collectWorks(): List<WorkLineRequestDto>? {
        val works = mutableListOf<WorkLineRequestDto>()
        for (i in 0 until binding.worksContainer.childCount) {
            val row = ItemWorkEditRowBinding.bind(binding.worksContainer.getChildAt(i))
            val name = row.workName.text?.toString()?.trim().orEmpty()
            val rate = row.rate.text?.toString()?.trim()?.toDoubleOrNull()
            if (name.isNotEmpty() && rate != null && rate >= 0) works.add(WorkLineRequestDto(name, rate))
        }
        return works.ifEmpty { null }
    }

    private fun onSave() {
        val name = binding.nameInput.text?.toString()?.trim().orEmpty()
        if (name.isEmpty()) { toast("Enter a product name"); return }
        val works = collectWorks() ?: run { toast("Add at least one work with a rate"); return }
        val unit = binding.unitInput.text?.toString()?.trim().orEmpty().ifEmpty { "pieces" }
        viewModel.save(editingId, ProductRequestDto(name, unit, selectedIcon, works))
    }

    private fun confirmDelete() {
        val id = editingId ?: return
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete product?")
            .setMessage("This removes the product and its works from this company.")
            .setNegativeButton("Keep", null)
            .setPositiveButton("Delete") { _, _ -> viewModel.delete(id) }
            .show()
    }

    private fun observeSaveState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.saveState.collect { state ->
                    when (state) {
                        is ProductViewModel.SaveState.Loading -> {
                            binding.save.isEnabled = false
                            binding.progress.visibility = View.VISIBLE
                        }
                        is ProductViewModel.SaveState.Done -> {
                            viewModel.consumeSaveState(); dismiss()
                        }
                        is ProductViewModel.SaveState.Error -> {
                            binding.save.isEnabled = true
                            binding.progress.visibility = View.GONE
                            toast(state.message); viewModel.consumeSaveState()
                        }
                        is ProductViewModel.SaveState.Idle -> {
                            binding.save.isEnabled = true
                            binding.progress.visibility = View.GONE
                        }
                    }
                }
            }
        }
    }

    private fun fmtRate(v: Double) =
        if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')

    private fun toast(m: String) = Toast.makeText(requireContext(), m, Toast.LENGTH_SHORT).show()

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}