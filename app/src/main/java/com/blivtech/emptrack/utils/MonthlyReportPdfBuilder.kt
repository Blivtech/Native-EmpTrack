package com.blivtech.emptrack.utils

import android.content.Context
import android.graphics.Color
import com.blivtech.emptrack.data.model.*
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object MonthlyReportPdfBuilder {

    private val ACCENT_OVERALL    = Color.parseColor("#0C447C")  // blue
    private val ACCENT_SHIFTWISE  = Color.parseColor("#27500A")  // green
    private val ACCENT_EMPLOYEE   = Color.parseColor("#633806")  // amber

    // ═══════════════════════════════════════════
    // ✅ 1. Overall Report PDF
    // ═══════════════════════════════════════════
    fun buildOverallPdf(
        context: Context,
        companyName: String,
        monthLabel: String,        // "June 2026"
        workingDays: Int,
        report: MonthlyReportDto
    ): File {
        val gen = PdfReportGenerator(
            context        = context,
            companyName    = companyName,
            reportTitle    = "Monthly attendance report — overall",
            periodLabel    = "$monthLabel · $workingDays working days",
            accentColor    = ACCENT_OVERALL
        )

        // ✅ Summary cards
        gen.drawSummaryCards(
            listOf(
                Triple("PRESENT", report.totalPresent.toString(), Color.parseColor("#27500A")),
                Triple("ABSENT",  report.totalAbsent.toString(),  Color.parseColor("#A32D2D")),
                Triple("HOLIDAY", report.totalHoliday.toString(), Color.parseColor("#444441")),
                Triple("WEEK OFF",report.totalWeekOff.toString(), Color.parseColor("#444441"))
            )
        )

        // ✅ Employee table
        val rows = report.employees.map { e ->
            listOf(
                e.empName,
                e.presentDays.toString(),
                e.absentDays.toString(),
                e.holidayDays.toString(),
                e.weekOffDays.toString(),
                e.totalDays.toString()
            )
        }
        val totalRow = listOf(
            "TOTAL",
            report.totalPresent.toString(),
            report.totalAbsent.toString(),
            report.totalHoliday.toString(),
            report.totalWeekOff.toString(),
            (report.totalPresent + report.totalAbsent + report.totalHoliday + report.totalWeekOff).toString()
        )

        gen.drawTable(
            headers    = listOf("Employee", "P", "A", "H", "WO", "Total"),
            colWeights = listOf(2.4f, 0.7f, 0.7f, 0.7f, 0.7f, 0.8f),
            rows       = rows,
            totalRow   = totalRow
        )

        val fileName = "Monthly_Overall_${monthLabel.replace(" ", "_")}.pdf"
        return gen.save(fileName)
    }

    // ═══════════════════════════════════════════
    // ✅ 2. Shift Wise Report PDF — one PDF, one section per shift
    // ═══════════════════════════════════════════
    fun buildShiftWisePdf(
        context: Context,
        companyName: String,
        monthLabel: String,
        workingDays: Int,
        shiftReports: List<MonthlyShiftReportDto>     // one per shift, already loaded
    ): File {
        val gen = PdfReportGenerator(
            context     = context,
            companyName = companyName,
            reportTitle = "Monthly attendance report — shift wise",
            periodLabel = "$monthLabel · $workingDays working days",
            accentColor = ACCENT_SHIFTWISE
        )

        shiftReports.forEach { shift ->
            gen.drawSectionBanner(
                text       = "${shiftEmoji(shift.shiftName)} ${shift.shiftName} · ${shift.totalEmployees} employees",
                bannerBg   = Color.parseColor("#EAF3DE"),
                bannerText = Color.parseColor("#27500A")
            )

            val rows = shift.employees.map { e ->
                listOf(
                    e.empName,
                    e.presentDays.toString(),
                    e.absentDays.toString(),
                    e.holidayDays.toString(),
                    e.weekOffDays.toString()
                )
            }

            gen.drawTable(
                headers    = listOf("Employee", "P", "A", "H", "WO"),
                colWeights = listOf(2.6f, 0.7f, 0.7f, 0.7f, 0.7f),
                rows       = rows
            )
        }

        val fileName = "Monthly_ShiftWise_${monthLabel.replace(" ", "_")}.pdf"
        return gen.save(fileName)
    }

    // ═══════════════════════════════════════════
    // ✅ 3. Employee Wise Report PDF — one PDF per employee, date/shift/status rows
    // ═══════════════════════════════════════════
    fun buildEmployeeWisePdf(
        context: Context,
        companyName: String,
        monthLabel: String,
        detail: MonthlyEmployeeDetail
    ): File {
        val gen = PdfReportGenerator(
            context     = context,
            companyName = companyName,
            reportTitle = "Monthly attendance report — employee wise",
            periodLabel = monthLabel,
            accentColor = ACCENT_EMPLOYEE
        )

        val initials = detail.empName.trim().split(" ")
            .mapNotNull { it.firstOrNull()?.toString() }
            .take(2).joinToString("")

        gen.drawEmployeeStrip(
            initials    = initials,
            nameLine    = "${detail.empName} · $detail.empCode",
            metaLine    = "${detail.deptName} · ${detail.desgName} · " +
                          "Present ${detail.presentDays} · Absent ${detail.absentDays} · ${detail.attendancePercent}%",
            avatarColor = ACCENT_EMPLOYEE
        )
//
//        // ✅ Build date/shift/status rows from dailyStatus list
//        val rows = detail.dailyStatus.map { d ->
//            listOf(formatDateDay(d.date, d.dayName), detail.shiftName, d.statusLabel)
//        }

        gen.drawTable(
            headers         = listOf("Date", "Shift", "Status"),
            colWeights      = listOf(1.4f, 1.2f, 1f),
            rows            = emptyList(),
            statusColIndex  = 2,
            statusColors    = { status ->
                when (status) {
                    "Present" -> Color.parseColor("#EAF3DE") to Color.parseColor("#27500A")
                    "Late"    -> Color.parseColor("#FAEEDA") to Color.parseColor("#633806")
                    "Absent"  -> Color.parseColor("#FCEBEB") to Color.parseColor("#A32D2D")
                    "Holiday" -> Color.parseColor("#EEEDFE") to Color.parseColor("#3C3489")
                    "Week Off"-> Color.parseColor("#F1EFE8") to Color.parseColor("#5F5E5A")
                    else      -> Color.LTGRAY to Color.DKGRAY
                }
            }
        )

        val fileName = "Monthly_${detail.empName.replace(" ", "_")}_${monthLabel.replace(" ", "_")}.pdf"
        return gen.save(fileName)
    }

    // ─────────────────────────────────
    // ✅ Helpers
    // ─────────────────────────────────
    private fun shiftEmoji(name: String) = when {
        name.contains("morning", true) || name.contains("day", true) -> "☀"
        name.contains("evening", true) -> "☁"
        name.contains("night",   true) -> "☾"
        else -> "○"
    }

    private fun formatDateDay(date: String, dayName: String): String {
        return try {
            val inp = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val out = SimpleDateFormat("d MMM", Locale.getDefault())
            "${out.format(inp.parse(date) ?: Date())}, $dayName"
        } catch (e: Exception) { "$date, $dayName" }
    }
}