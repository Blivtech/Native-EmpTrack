package com.blivtech.emptrack.ui.entry

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.databinding.ItemProductTileBinding
import java.util.Locale

class ProductTileAdapter(
    private val onClick: (ProductWithWorks) -> Unit
) : ListAdapter<ProductWithWorks, ProductTileAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val b = ItemProductTileBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(b)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    inner class VH(private val b: ItemProductTileBinding) : RecyclerView.ViewHolder(b.root) {
        fun bind(item: ProductWithWorks) {
            b.monogram.text = item.product.name.trim().take(1).uppercase(Locale.getDefault())
            b.name.text = item.product.name
            b.works.text = "${item.works.size} works"
            b.root.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<ProductWithWorks>() {
            override fun areItemsTheSame(a: ProductWithWorks, b: ProductWithWorks) = a.product.id == b.product.id
            override fun areContentsTheSame(a: ProductWithWorks, b: ProductWithWorks) = a == b
        }
    }
}