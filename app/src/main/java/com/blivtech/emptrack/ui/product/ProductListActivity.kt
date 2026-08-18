package com.blivtech.emptrack.ui.product

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.databinding.ActivityProductListBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProductListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProductListBinding
    private val viewModel: ProductViewModel by viewModels()
    private val adapter by lazy { ProductAdapter(onClick = ::openEdit) }

    private val btCode by lazy { intent.getStringExtra(EXTRA_BT_CODE).orEmpty() }
    private val companyCode by lazy { intent.getStringExtra(EXTRA_COMPANY_CODE).orEmpty() }
    private val companyName by lazy { intent.getStringExtra(EXTRA_COMPANY_NAME).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setScope(btCode, companyCode)

        binding.subtitle.text = companyName.ifBlank { companyCode }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.back.setOnClickListener { finish() }
        binding.addButton.setOnClickListener { openAdd() }
        binding.addCard.setOnClickListener { openAdd() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.products.collect { list ->
                    adapter.submitList(list)
                    binding.count.text = list.size.toString()
                    binding.emptyState.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }

    private fun openAdd() {
        viewModel.startAdd()
        ProductEditBottomSheet().show(supportFragmentManager, "product_edit")
    }

    private fun openEdit(item: ProductWithWorks) {
        viewModel.startEdit(item)
        ProductEditBottomSheet().show(supportFragmentManager, "product_edit")
    }

    companion object {
        private const val EXTRA_BT_CODE = "btCode"
        private const val EXTRA_COMPANY_CODE = "companyCode"
        private const val EXTRA_COMPANY_NAME = "companyName"

        fun newIntent(context: Context, btCode: String, companyCode: String, companyName: String) =
            Intent(context, ProductListActivity::class.java).apply {
                putExtra(EXTRA_BT_CODE, btCode)
                putExtra(EXTRA_COMPANY_CODE, companyCode)
                putExtra(EXTRA_COMPANY_NAME, companyName)
            }
    }
}