package com.blivtech.emptrack.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blivtech.emptrack.data.model.CardDetailsForHomeActivity
import com.blivtech.emptrack.databinding.ItemListModuleCardBinding

class ModuleCardAdapter(
    private val items: List<CardDetailsForHomeActivity>,
    private val onClick: (CardDetailsForHomeActivity) -> Unit
) : RecyclerView.Adapter<ModuleCardAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemListModuleCardBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, position: Int): ViewHolder {
        val binding = ItemListModuleCardBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        with(holder.binding) {
            tvModuleName.text = item.cardName
            tvModuleSub.text = item.subtitle
            ivModuleIcon.setImageResource(item.iconRes)
            layoutModuleIcon.setBackgroundResource(item.bgColorRes)

            root.setOnClickListener { onClick(item) }
        }
    }

    override fun getItemCount() = items.size
}