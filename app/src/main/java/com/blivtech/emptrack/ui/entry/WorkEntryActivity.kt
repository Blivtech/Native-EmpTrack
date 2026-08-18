package com.blivtech.emptrack.ui.entry

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.data.local.entity.ProductWithWorks
import com.blivtech.emptrack.data.model.WorkEntry
import com.blivtech.emptrack.databinding.ActivityWorkEntryBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

@AndroidEntryPoint
class WorkEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWorkEntryBinding
    private val viewModel: WorkEntryViewModel by viewModels()

    private val productAdapter by lazy { ProductTileAdapter(::openAdd) }
    private val entryAdapter by lazy { EntryAdapter(::openEdit) }

    private val btCode by lazy { intent.getStringExtra(EXTRA_BT).orEmpty() }
    private val companyCode by lazy { intent.getStringExtra(EXTRA_COMPANY).orEmpty() }
    private val employeeId by lazy { intent.getLongExtra(EXTRA_EMP_ID, 0L) }
    private val employeeName by lazy { intent.getStringExtra(EXTRA_EMP_NAME).orEmpty() }
    private val entryDate by lazy { intent.getStringExtra(EXTRA_DATE).orEmpty() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWorkEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setScope(btCode, companyCode, employeeId, entryDate)

        binding.workerName.text = employeeName
        binding.dateText.text = prettyDate(entryDate)
        binding.back.setOnClickListener { finish() }
        binding.doneButton.setOnClickListener { viewModel.saveAll() }   // Done = save to API

        binding.productRecycler.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.productRecycler.adapter = productAdapter

        binding.entryRecycler.layoutManager = LinearLayoutManager(this)
        binding.entryRecycler.adapter = entryAdapter

        observe()
    }

    private fun observe() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.products.collect { productAdapter.submitList(it) } }
                launch {
                    viewModel.entries.collect {
                        entryAdapter.submitList(it)
                        binding.emptyEntries.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
                launch {
                    viewModel.totals.collect {
                        binding.totalPieces.text = "${it.pieces} pcs"
                        binding.totalAmount.text = "₹${fmt(it.amount)}"
                    }
                }
                // Optional: bind a ProgressBar to loading/saving in your layout if you have one.
                launch { viewModel.saving.collect { binding.doneButton.isEnabled = !it } }
                launch {
                    viewModel.result.collect { r ->
                        when (r) {
                            is WorkEntryViewModel.Result.Saved -> { setResult(RESULT_OK); finish() }
                            is WorkEntryViewModel.Result.Fail ->
                                Toast.makeText(this@WorkEntryActivity, r.message, Toast.LENGTH_SHORT).show()
                            null -> Unit
                        }
                        if (r != null) viewModel.consumeResult()
                    }
                }
            }
        }
    }

    private fun openAdd(product: ProductWithWorks) {
        viewModel.startAdd(product)
        EntryCounterBottomSheet().show(supportFragmentManager, "entry_counter")
    }

    private fun openEdit(entry: WorkEntry) {
        viewModel.startEdit(entry)
        EntryCounterBottomSheet().show(supportFragmentManager, "entry_counter")
    }

    private fun prettyDate(iso: String): String = try {
        val parsed = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso)
        SimpleDateFormat("EEE, d MMM yyyy", Locale.getDefault()).format(parsed!!)
    } catch (e: Exception) { iso }

    private fun fmt(v: Double) =
        if (v % 1.0 == 0.0) v.toInt().toString() else v.toString().trimEnd('0').trimEnd('.')

    companion object {
        private const val EXTRA_BT = "btCode"
        private const val EXTRA_COMPANY = "companyCode"
        private const val EXTRA_EMP_ID = "employeeId"
        private const val EXTRA_EMP_NAME = "employeeName"
        private const val EXTRA_DATE = "entryDate"

        fun newIntent(
            context: Context, btCode: String, companyCode: String,
            employeeId: Long, employeeName: String, entryDate: String
        ) = Intent(context, WorkEntryActivity::class.java).apply {
            putExtra(EXTRA_BT, btCode)
            putExtra(EXTRA_COMPANY, companyCode)
            putExtra(EXTRA_EMP_ID, employeeId)
            putExtra(EXTRA_EMP_NAME, employeeName)
            putExtra(EXTRA_DATE, entryDate)
        }
    }
}