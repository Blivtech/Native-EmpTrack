package com.blivtech.emptrack.ui.workers

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.databinding.ItemWorkerBinding
import java.util.Locale

class WorkerAdapter(
    private val onClick: (WorkersViewModel.WorkerRow) -> Unit
) : ListAdapter<WorkersViewModel.WorkerRow, WorkerAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemWorkerBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemWorkerBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(w: WorkersViewModel.WorkerRow) {
            b.monogram.text = w.name.trim().take(1).uppercase(Locale.getDefault())
            b.name.text = w.name
            b.role.text = w.role
            if (w.pieces > 0) {
                b.status.text = "${w.pieces} pcs · ₹${fmt(w.amount)}"
                b.status.setTextColor(0xFF0E9F55.toInt())
            } else {
                b.status.text = "No entries yet"
                b.status.setTextColor(0xFF6C7A96.toInt())
            }
            b.root.setOnClickListener { onClick(w) }
        }
        private fun fmt(v: Double) =
            if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WorkersViewModel.WorkerRow>() {
            override fun areItemsTheSame(a: WorkersViewModel.WorkerRow, b: WorkersViewModel.WorkerRow) = a.id == b.id
            override fun areContentsTheSame(a: WorkersViewModel.WorkerRow, b: WorkersViewModel.WorkerRow) = a == b
        }
    }
}