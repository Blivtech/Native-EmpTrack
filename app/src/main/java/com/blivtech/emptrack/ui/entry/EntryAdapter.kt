package com.blivtech.emptrack.ui.entry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.WorkEntry
import com.blivtech.emptrack.databinding.ItemEntryBinding

class EntryAdapter(
    private val onClick: (WorkEntry) -> Unit
) : ListAdapter<WorkEntry, EntryAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemEntryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemEntryBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(e: WorkEntry) {
            b.title.text = "${e.productName} · ${e.workName}"
            b.sub.text = "${e.pieces} × ₹${fmt(e.rate)}"
            b.amount.text = "₹${fmt(e.amount)}"
            b.root.setOnClickListener { onClick(e) }
        }
        private fun fmt(v: Double) =
            if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<WorkEntry>() {
            override fun areItemsTheSame(a: WorkEntry, b: WorkEntry) = a.clientId == b.clientId
            override fun areContentsTheSame(a: WorkEntry, b: WorkEntry) = a == b
        }
    }
}