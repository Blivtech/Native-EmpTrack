package com.blivtech.emptrack.utils

import android.content.Context
import android.graphics.pdf.PdfDocument
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.blivtech.emptrack.R
import com.blivtech.emptrack.data.model.MonthlyEmployeeSummaryDto
import com.blivtech.emptrack.data.model.MonthlyReportData
import com.blivtech.emptrack.data.model.MonthlyReportDto
import com.blivtech.emptrack.data.model.MonthlyShiftReportDto
import java.io.File
import java.io.FileOutputStream
import kotlin.math.ceil

/**
 * Renders the DESIGNED report layout (pdf_report_page.xml) to a multi-page A4 PDF.
 * Company name + address are STATIC inside pdf_report_page.xml — edit them there.
 * Signatures match what MonthlyReportActivity already calls.
 */
object MonthlyReportPdfBuilder {

    private const val PAGE_W = 1080                          // px; layout is measured at this width
    private val PAGE_H = (PAGE_W * 842f / 595f).toInt()      // A4 portrait ratio ≈ 1528

    // ── OVERALL ──────────────────────────────────────────────
    fun buildOverallPdf(
        context: Context,
        companyName: String,          // kept for compatibility; header is static (see note)
        monthLabel: String,
        workingDays: Int,
        report: MonthlyReportDto
    ): File {
        val page = inflatePage(context)

        page.findViewById<TextView>(R.id.tvReportTitle).text = "Overall Monthly Report"
        page.findViewById<TextView>(R.id.tvReportMeta).text =
            "Month · $monthLabel\nWorking days · $workingDays\nGenerated · ${today()}"
        page.findViewById<TextView>(R.id.tvReportSubtitle).text =
            "Overall Report · ${report.totalEmployees} employees"

        val container = page.findViewById<LinearLayout>(R.id.sectionsContainer)
        addSection(context, container, heading = null, employees = report.employees)

        bindSummary(page, report.totalPresent, report.totalAbsent, report.totalHoliday, report.totalWeekOff)

        return render(context, page, "overall_report_${safe(monthLabel)}.pdf")
    }

    // ── SHIFT-WISE ───────────────────────────────────────────
    fun buildShiftWisePdf(
        context: Context,
        companyName: String,
        monthLabel: String,
        workingDays: Int,
        shiftReports: List<MonthlyShiftReportDto>
    ): File {
        val page = inflatePage(context)

        page.findViewById<TextView>(R.id.tvReportTitle).text = "Shift-Wise Monthly Report"
        val totalEmp = shiftReports.sumOf { it.employees.size }
        page.findViewById<TextView>(R.id.tvReportMeta).text =
            "Month · $monthLabel\nWorking days · $workingDays\nGenerated · ${today()}"
        page.findViewById<TextView>(R.id.tvReportSubtitle).text =
            "Shift-Wise Report · ${shiftReports.size} shifts · $totalEmp employees"

        val container = page.findViewById<LinearLayout>(R.id.sectionsContainer)
        shiftReports.forEach { rep ->
            val heading = rep.employees.firstOrNull()?.shiftName ?: "Shift"
            addSection(context, container, heading = heading, employees = rep.employees)
        }

        // combined summary
        bindSummary(
            page,
            shiftReports.sumOf { it.totalPresent },
            shiftReports.sumOf { it.totalAbsent },
            shiftReports.sumOf { it.totalHoliday },
            shiftReports.sumOf { it.totalWeekOff }
        )

        return render(context, page, "shiftwise_report_${safe(monthLabel)}.pdf")
    }

    // ── build one table section ──────────────────────────────
    private fun addSection(
        context: Context,
        container: LinearLayout,
        heading: String?,
        employees: List<MonthlyEmployeeSummaryDto>
    ) {
        val inflater = LayoutInflater.from(context)
        val section = inflater.inflate(R.layout.pdf_report_section, container, false)

        val head = section.findViewById<TextView>(R.id.tvShiftHeading)
        if (heading != null) { head.visibility = View.VISIBLE; head.text = heading }
        else head.visibility = View.GONE

        val rows = section.findViewById<LinearLayout>(R.id.rowsContainer)
        employees.forEach { emp ->
            val row = inflater.inflate(R.layout.item_overall_report_row, rows, false)
            row.findViewById<TextView>(R.id.tvName).text = emp.empName
            row.findViewById<TextView>(R.id.tvSub).text =
                listOf(emp.empCode, emp.deptName, emp.shiftName).filter { it.isNotBlank() }.joinToString(" · ")
            row.findViewById<TextView>(R.id.tvP).text     = emp.presentDays.toString()
            row.findViewById<TextView>(R.id.tvL).text     = emp.absentDays.toString()
            row.findViewById<TextView>(R.id.tvH).text     = emp.holidayDays.toString()
            row.findViewById<TextView>(R.id.tvWO).text    = emp.weekOffDays.toString()
            row.findViewById<TextView>(R.id.tvTotal).text = emp.totalDays.toString()
            rows.addView(row)
        }

        section.findViewById<TextView>(R.id.tvSubP).text     = employees.sumOf { it.presentDays }.toString()
        section.findViewById<TextView>(R.id.tvSubL).text     = employees.sumOf { it.absentDays }.toString()
        section.findViewById<TextView>(R.id.tvSubH).text     = employees.sumOf { it.holidayDays }.toString()
        section.findViewById<TextView>(R.id.tvSubWO).text    = employees.sumOf { it.weekOffDays }.toString()
        section.findViewById<TextView>(R.id.tvSubTotal).text = employees.sumOf { it.totalDays }.toString()

        container.addView(section)
    }

    private fun bindSummary(page: View, p: Int, a: Int, h: Int, w: Int) {
        page.findViewById<TextView>(R.id.tvSumPresent).text = p.toString()
        page.findViewById<TextView>(R.id.tvSumAbsent).text  = a.toString()
        page.findViewById<TextView>(R.id.tvSumHoliday).text = h.toString()
        page.findViewById<TextView>(R.id.tvSumWeekOff).text = w.toString()
    }

    private fun inflatePage(context: Context): View =
        LayoutInflater.from(context).inflate(R.layout.pdf_report_page, null, false)

    // ── measure + paginate + write ───────────────────────────
    private fun render(context: Context, page: View, fileName: String): File {
        page.measure(
            View.MeasureSpec.makeMeasureSpec(PAGE_W, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        )
        page.layout(0, 0, page.measuredWidth, page.measuredHeight)

        val totalH   = page.measuredHeight
        val pageH    = PAGE_H
        val pageCount = ceil(totalH.toDouble() / pageH).toInt().coerceAtLeast(1)

        val pdf = PdfDocument()
        for (i in 0 until pageCount) {
            val info = PdfDocument.PageInfo.Builder(PAGE_W, pageH, i + 1).create()
            val pdfPage = pdf.startPage(info)
            val canvas = pdfPage.canvas
            canvas.save()
            canvas.translate(0f, (-i * pageH).toFloat())
            page.draw(canvas)
            canvas.restore()
            pdf.finishPage(pdfPage)
        }

        val dir  = File(context.cacheDir, "reports").apply { mkdirs() }
        val file = File(dir, fileName)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()
        return file
    }

    private fun today(): String =
        java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
            .format(java.util.Date())

    private fun safe(s: String) = s.replace("[^A-Za-z0-9]".toRegex(), "_")
}
