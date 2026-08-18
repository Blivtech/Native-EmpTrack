package com.blivtech.emptrack.ui.product

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.databinding.ItemProductBinding
import java.util.Locale

class ProductAdapter(
    private val onClick: (ProductWithWorks) -> Unit
) : ListAdapter<ProductWithWorks, ProductAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProductBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemProductBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ProductWithWorks) {
            val p = item.product
            b.monogram.text = p.name.trim().take(1).uppercase(Locale.getDefault())
            b.name.text = p.name
            b.unit.text = p.unit
            b.works.text = "${item.works.size} works"
            b.rate.text = rateRange(item)
            b.root.setOnClickListener { onClick(item) }
        }

        private fun rateRange(item: ProductWithWorks): String {
            if (item.works.isEmpty()) return "—"
            val rates = item.works.map { it.rate }
            val min = rates.min(); val max = rates.max()
            return if (min == max) "₹${fmt(min)}/pc" else "₹${fmt(min)}–₹${fmt(max)}/pc"
        }

        private fun fmt(v: Double): String =
            if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductWithWorks>() {
            override fun areItemsTheSame(a: ProductWithWorks, b: ProductWithWorks) =
                a.product.id == b.product.id
            override fun areContentsTheSame(a: ProductWithWorks, b: ProductWithWorks) = a == b
        }
    }
}