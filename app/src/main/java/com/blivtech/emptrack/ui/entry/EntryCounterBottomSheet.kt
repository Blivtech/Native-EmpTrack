package com.blivtech.emptrack.ui.entry

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import com.blivtech.emptrack.data.local.entity.ProductWorkEntity
import com.blivtech.emptrack.databinding.SheetEntryCounterBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class EntryCounterBottomSheet : BottomSheetDialogFragment() {

    private var _binding: SheetEntryCounterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: WorkEntryViewModel by activityViewModels()

    // resolved selection
    private var productId = 0L
    private var productName = ""
    private var unit = "pieces"
    private var selectedWork: ProductWorkEntity? = null
    private var editing = false
    private var pieces = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = SheetEntryCounterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        when (val target = viewModel.sheet.value) {
            is WorkEntryViewModel.SheetTarget.Add -> setupAdd(target)
            is WorkEntryViewModel.SheetTarget.Edit -> setupEdit(target)
            null -> { dismiss(); return }
        }

        binding.minus.setOnClickListener { setPieces(pieces - 1) }
        binding.plus.setOnClickListener { setPieces(pieces + 1) }
        binding.add5.setOnClickListener { setPieces(pieces + 5) }
        binding.add10.setOnClickListener { setPieces(pieces + 10) }
        binding.add25.setOnClickListener { setPieces(pieces + 25) }
        binding.close.setOnClickListener { dismiss() }
        binding.cancel.setOnClickListener { dismiss() }
        binding.save.setOnClickListener { onSave() }
        binding.delete.setOnClickListener { confirmDelete() }
    }

    private fun setupAdd(target: WorkEntryViewModel.SheetTarget.Add) {
        val product = target.product
        productId = product.product.id
        productName = product.product.name
        unit = product.product.unit
        editing = false

        binding.title.text = productName
        binding.subtitle.text = "Choose work, then count"
        binding.delete.visibility = View.GONE
        binding.counterGroup.visibility = View.GONE

        binding.workChips.removeAllViews()
        product.works.forEach { w ->
            val chip = Chip(requireContext()).apply {
                text = "${w.workName}  ₹${fmt(w.rate)}"
                isCheckable = true
                setOnClickListener {
                    selectedWork = w
                    binding.subtitle.text = "${w.workName} · ₹${fmt(w.rate)}/pc"
                    binding.counterGroup.visibility = View.VISIBLE
                    setPieces(if (pieces == 0) 1 else pieces)
                }
            }
            binding.workChips.addView(chip)
        }
        setPieces(0)
    }

    private fun setupEdit(target: WorkEntryViewModel.SheetTarget.Edit) {
        val e = target.entry
        editing = true
        productId = e.productId
        productName = e.productName
        unit = e.unit
        selectedWork = ProductWorkEntity(
            productId = e.productId, btCode = e.btCode, companyCode = e.companyCode,
            workTypeId = e.workTypeId, workCode = null, workName = e.workName, rate = e.rate
        )

        binding.title.text = productName
        binding.subtitle.text = "${e.workName} · ₹${fmt(e.rate)}/pc"
        binding.workChips.visibility = View.GONE
        binding.counterGroup.visibility = View.VISIBLE
        binding.delete.visibility = View.VISIBLE
        setPieces(e.pieces)
    }

    private fun setPieces(value: Int) {
        pieces = value.coerceAtLeast(0)
        binding.count.text = pieces.toString()
        val rate = selectedWork?.rate ?: 0.0
        binding.amount.text = "= ₹${fmt(pieces * rate)}"
    }

    private fun onSave() {
        val work = selectedWork ?: run { binding.subtitle.text = "Pick a work first"; return }
        viewModel.upsertEntry(
            productId = productId, productName = productName, unit = unit,
            workTypeId = work.workTypeId, workName = work.workName, rate = work.rate, pieces = pieces
        )
        dismiss()
    }

    private fun confirmDelete() {
        val target = viewModel.sheet.value as? WorkEntryViewModel.SheetTarget.Edit ?: return
        val e = target.entry
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Delete entry?")
            .setMessage("Remove ${e.productName} · ${e.workName} (${e.pieces} pcs)?")
            .setNegativeButton("Keep", null)
            .setPositiveButton("Delete") { _, _ -> viewModel.deleteEntry(e); dismiss() }
            .show()
    }

    private fun fmt(v: Double) =
        if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')

    override fun onDismiss(dialog: android.content.DialogInterface) {
        super.onDismiss(dialog)
        viewModel.clearSheet()
    }

    override fun onDestroyView() { super.onDestroyView(); _binding = null }
}