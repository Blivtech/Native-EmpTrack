package com.blivtech.emptrack.ui.workers

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.blivtech.emptrack.databinding.ActivityDailyEntryBinding
import com.blivtech.emptrack.ui.entry.WorkEntryActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/** Daily Entry screen: pick a date, see the company's workers with that day's totals, tap to log. */
@AndroidEntryPoint
class DailyEntryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyEntryBinding
    private val viewModel: WorkersViewModel by viewModels()
    private val adapter by lazy { WorkerAdapter(::openWorker) }

    private val btCode by lazy { intent.getStringExtra(EXTRA_BT).orEmpty() }
    private val companyCode by lazy { intent.getStringExtra(EXTRA_COMPANY).orEmpty() }
    private val companyName by lazy { intent.getStringExtra(EXTRA_COMPANY_NAME).orEmpty() }

    // Re-fetch day totals from the API when we come back from the entry screen.
    private val workEntryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refresh() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDailyEntryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel.setCompany(companyCode)
        binding.subtitle.text = companyName.ifBlank { companyCode }
        binding.back.setOnClickListener { finish() }

        binding.recycler.layoutManager = LinearLayoutManager(this)
        binding.recycler.adapter = adapter

        binding.datePill.setOnClickListener { pickDate() }

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.date.collect { binding.dateText.text = pretty(it) } }
                launch {
                    viewModel.workers.collect {
                        adapter.submitList(it)
                        binding.empty.visibility = if (it.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    private fun openWorker(w: WorkersViewModel.WorkerRow) {
        workEntryLauncher.launch(
            WorkEntryActivity.newIntent(
                context = this, btCode = btCode, companyCode = companyCode,
                employeeId = w.id, employeeName = w.name, entryDate = viewModel.date.value
            )
        )
    }

    private fun pickDate() {
        val cal = Calendar.getInstance()
        runCatching {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(viewModel.date.value)
        }.getOrNull()?.let { cal.time = it }
        DatePickerDialog(
            this,
            { _, y, m, d -> viewModel.setDate("%04d-%02d-%02d".format(y, m + 1, d)) },
            cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)
        ).apply { datePicker.maxDate = System.currentTimeMillis() }.show()
    }

    private fun pretty(iso: String) = try {
        val p = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(iso)
        SimpleDateFormat("EEE, d MMM", Locale.getDefault()).format(p!!)
    } catch (e: Exception) { iso }

    companion object {
        private const val EXTRA_BT = "btCode"
        private const val EXTRA_COMPANY = "companyCode"
        private const val EXTRA_COMPANY_NAME = "companyName"
        fun newIntent(context: Context, btCode: String, companyCode: String, companyName: String) =
            Intent(context, DailyEntryActivity::class.java).apply {
                putExtra(EXTRA_BT, btCode)
                putExtra(EXTRA_COMPANY, companyCode)
                putExtra(EXTRA_COMPANY_NAME, companyName)
            }
    }
}