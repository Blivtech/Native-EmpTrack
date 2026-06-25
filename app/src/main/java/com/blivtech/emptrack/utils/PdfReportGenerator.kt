package com.blivtech.emptrack.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ Shared PDF drawing engine — A4 page, header/footer, table renderer.
 * All 3 report PDFs (Overall / Shift wise / Employee wise) build on this.
 */
class PdfReportGenerator(
    private val context: Context,
    private val companyName: String,
    private val reportTitle: String,      // e.g. "Monthly Attendance Report — Overall"
    private val periodLabel: String,      // e.g. "June 2026 · 26 working days"
    private val accentColor: Int          // header / table-header color
) {
    // ✅ A4 size at 72dpi-ish scale (matches PdfDocument convention)
    private val pageWidth  = 595
    private val pageHeight = 842
    private val marginX    = 32f
    private var marginTop  = 40f
    private val marginBottom = 50f

    private val document = PdfDocument()
    private var pageNumber = 0
    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private var y = 0f

    private val timeFmt = SimpleDateFormat("d MMM yyyy, HH:mm", Locale.getDefault())

    // ─────────────────────────────────
    // ✅ Paints
    // ─────────────────────────────────
    private val titlePaint = Paint().apply {
        color = accentColor
        textSize = 18f
        isFakeBoldText = true
        isAntiAlias = true
    }
    private val subtitlePaint = Paint().apply {
        color = Color.parseColor("#5F5E5A")
        textSize = 11f
        isAntiAlias = true
    }
    private val periodPaint = Paint().apply {
        color = Color.parseColor("#888780")
        textSize = 9.5f
        isAntiAlias = true
    }
    private val sectionPaint = Paint().apply {
        color = accentColor
        textSize = 11f
        isFakeBoldText = true
        isAntiAlias = true
    }
    private val tableHeaderPaint = Paint().apply {
        color = Color.WHITE
        textSize = 9.5f
        isFakeBoldText = true
        isAntiAlias = true
    }
    private val tableCellPaint = Paint().apply {
        color = Color.parseColor("#2C2C2A")
        textSize = 9.5f
        isAntiAlias = true
    }
    private val tableCellBoldPaint = Paint().apply {
        color = Color.parseColor("#2C2C2A")
        textSize = 9.5f
        isFakeBoldText = true
        isAntiAlias = true
    }
    private val footerPaint = Paint().apply {
        color = Color.parseColor("#888780")
        textSize = 8f
        isAntiAlias = true
    }
    private val linePaint = Paint().apply {
        color = Color.parseColor("#D3D1C7")
        strokeWidth = 0.7f
    }

    // ─────────────────────────────────
    // ✅ Lifecycle — start first page
    // ─────────────────────────────────
    init {
        startNewPage()
        drawHeader()
    }

    private fun startNewPage() {
        pageNumber++
        val info = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        page   = document.startPage(info)
        canvas = page.canvas
        y      = marginTop
    }

    // ─────────────────────────────────
    // ✅ Header — drawn once per page (full on page 1, compact after)
    // ─────────────────────────────────
    private fun drawHeader() {
        canvas.drawText(companyName, marginX, y, titlePaint)
        y += 18f
        canvas.drawText(reportTitle, marginX, y, subtitlePaint)
        y += 14f
        canvas.drawText(periodLabel, marginX, y, periodPaint)
        y += 10f
        canvas.drawLine(marginX, y, pageWidth - marginX, y, Paint().apply {
            color = accentColor; strokeWidth = 1.5f
        })
        y += 18f
    }

    // ✅ Ensures enough vertical space remains, else starts a new page + redraws header
    private fun ensureSpace(needed: Float) {
        if (y + needed > pageHeight - marginBottom) {
            finishPageFooter()
            document.finishPage(page)
            startNewPage()
            drawHeader()
        }
    }

    // ─────────────────────────────────
    // ✅ Section banner — e.g. "Morning shift · 08:00–17:00 · 15 employees"
    // ─────────────────────────────────
    fun drawSectionBanner(text: String, bannerBg: Int, bannerText: Int) {
        ensureSpace(28f)
        val rect = RectF(marginX, y, pageWidth - marginX, y + 20f)
        canvas.drawRoundRect(rect, 4f, 4f, Paint().apply { color = bannerBg })
        val tp = Paint().apply {
            color = bannerText; textSize = 10f; isFakeBoldText = true; isAntiAlias = true
        }
        canvas.drawText(text, marginX + 10f, y + 14f, tp)
        y += 30f
    }

    // ─────────────────────────────────
    // ✅ Summary cards row — 3 or 4 metric boxes
    // ─────────────────────────────────
    fun drawSummaryCards(cards: List<Triple<String, String, Int>>) {
        // Triple<label, value, color>
        ensureSpace(50f)
        val gap = 8f
        val cardW = (pageWidth - 2 * marginX - gap * (cards.size - 1)) / cards.size
        var x = marginX
        cards.forEach { (label, value, color) ->
            val rect = RectF(x, y, x + cardW, y + 40f)
            canvas.drawRoundRect(rect, 4f, 4f, Paint().apply {
                this.color = Color.argb(28, Color.red(color), Color.green(color), Color.blue(color))
            })
            val labelPaint = Paint().apply {
                this.color = color; textSize = 8f; isAntiAlias = true
            }
            val valuePaint = Paint().apply {
                this.color = color; textSize = 14f; isFakeBoldText = true; isAntiAlias = true
            }
            canvas.drawText(label, x + cardW / 2 - labelPaint.measureText(label) / 2, y + 15f, labelPaint)
            canvas.drawText(value, x + cardW / 2 - valuePaint.measureText(value) / 2, y + 32f, valuePaint)
            x += cardW + gap
        }
        y += 50f
    }

    // ─────────────────────────────────
    // ✅ Table — header row + striped body rows + optional total row
    // colWeights sum doesn't need to be 1 — relative widths
    // ─────────────────────────────────
    fun drawTable(
        headers: List<String>,
        colWeights: List<Float>,
        rows: List<List<String>>,
        totalRow: List<String>? = null,
        statusColIndex: Int = -1,                 // optional: render this col as a colored pill
        statusColors: (String) -> Pair<Int, Int> = { Color.GRAY to Color.WHITE }
    ) {
        val tableWidth = pageWidth - 2 * marginX
        val totalWeight = colWeights.sum()
        val colWidths = colWeights.map { tableWidth * (it / totalWeight) }

        // ✅ Header row
        ensureSpace(24f)
        drawTableHeaderRow(headers, colWidths)

        // ✅ Body rows — striped, page-break aware
        rows.forEachIndexed { idx, row ->
            ensureSpace(22f).also {
                // if a page break happened mid-table, redraw header on new page
            }
            // re-check: if ensureSpace triggered a new page, header context is lost — redraw table header
            if (y == marginTop + 60f) { // heuristic: right after a fresh drawHeader()
                drawTableHeaderRow(headers, colWidths)
            }
            drawTableRow(row, colWidths, idx % 2 == 1, statusColIndex, statusColors)
        }

        // ✅ Total row
        totalRow?.let {
            ensureSpace(24f)
            drawTableTotalRow(it, colWidths)
        }

        y += 12f
    }

    private fun drawTableHeaderRow(headers: List<String>, colWidths: List<Float>) {
        val rowH = 22f
        canvas.drawRect(marginX, y, pageWidth - marginX, y + rowH, Paint().apply { color = accentColor })
        var x = marginX + 6f
        headers.forEachIndexed { i, h ->
            canvas.drawText(h, x, y + 15f, tableHeaderPaint)
            x += colWidths[i]
        }
        y += rowH
    }

    private fun drawTableRow(
        row: List<String>, colWidths: List<Float>, striped: Boolean,
        statusColIndex: Int, statusColors: (String) -> Pair<Int, Int>
    ) {
        val rowH = 20f
        if (striped) {
            canvas.drawRect(marginX, y, pageWidth - marginX, y + rowH, Paint().apply {
                color = Color.parseColor("#F1EFE8")
            })
        }
        var x = marginX + 6f
        row.forEachIndexed { i, cell ->
            if (i == statusColIndex) {
                val (bg, text) = statusColors(cell)
                val pillW = tableCellPaint.measureText(cell) + 16f
                val pillRect = RectF(x, y + 3f, x + pillW, y + rowH - 3f)
                canvas.drawRoundRect(pillRect, 6f, 6f, Paint().apply { color = bg })
                val pillPaint = Paint().apply {
                    color = text; textSize = 8.5f; isAntiAlias = true
                }
                canvas.drawText(cell, x + 8f, y + 14f, pillPaint)
            } else {
                val isNumeric = cell.toDoubleOrNull() != null
                val cellPaint = if (i == row.size - 1) tableCellBoldPaint else tableCellPaint
                val drawX = if (isNumeric) x + colWidths[i] / 2 - cellPaint.measureText(cell) / 2 else x
                canvas.drawText(cell, drawX, y + 14f, cellPaint)
            }
            x += colWidths[i]
        }
        canvas.drawLine(marginX, y + rowH, pageWidth - marginX, y + rowH, linePaint)
        y += rowH
    }

    private fun drawTableTotalRow(row: List<String>, colWidths: List<Float>) {
        val rowH = 22f
        canvas.drawRect(marginX, y, pageWidth - marginX, y + rowH, Paint().apply { color = accentColor })
        var x = marginX + 6f
        val totalPaint = Paint().apply {
            color = Color.WHITE; textSize = 9.5f; isFakeBoldText = true; isAntiAlias = true
        }
        row.forEachIndexed { i, cell ->
            val isNumeric = i > 0
            val drawX = if (isNumeric) x + colWidths[i] / 2 - totalPaint.measureText(cell) / 2 else x
            canvas.drawText(cell, drawX, y + 15f, totalPaint)
            x += colWidths[i]
        }
        y += rowH
    }

    // ─────────────────────────────────
    // ✅ Employee info strip — used in Employee-wise PDF
    // ─────────────────────────────────
    fun drawEmployeeStrip(
        initials: String, nameLine: String, metaLine: String, avatarColor: Int
    ) {
        ensureSpace(48f)
        val rect = RectF(marginX, y, pageWidth - marginX, y + 36f)
        canvas.drawRoundRect(rect, 4f, 4f, Paint().apply {
            color = Color.parseColor("#F1EFE8")
        })
        // avatar circle
        val cx = marginX + 20f; val cy = y + 18f
        canvas.drawCircle(cx, cy, 14f, Paint().apply {
            color = Color.argb(40, Color.red(avatarColor), Color.green(avatarColor), Color.blue(avatarColor))
        })
        val initPaint = Paint().apply {
            color = avatarColor; textSize = 10f; isFakeBoldText = true; isAntiAlias = true; textAlign = Paint.Align.CENTER
        }
        canvas.drawText(initials, cx, cy + 4f, initPaint)

        val namePaint = Paint().apply { color = Color.parseColor("#2C2C2A"); textSize = 11f; isFakeBoldText = true; isAntiAlias = true }
        val metaPaint = Paint().apply { color = Color.parseColor("#5F5E5A"); textSize = 9f; isAntiAlias = true }
        canvas.drawText(nameLine, marginX + 42f, y + 15f, namePaint)
        canvas.drawText(metaLine, marginX + 42f, y + 28f, metaPaint)

        y += 46f
    }

    fun drawSpacer(height: Float) { y += height }

    // ─────────────────────────────────
    // ✅ Finish — footer on every page, then save file, return Uri-able File
    // ─────────────────────────────────
    private fun finishPageFooter() {
        val footerY = pageHeight - 26f
        canvas.drawLine(marginX, footerY - 10f, pageWidth - marginX, footerY - 10f, linePaint)
        canvas.drawText(
            "Generated ${timeFmt.format(Date())}", marginX, footerY, footerPaint
        )
        val poweredBy = "Powered by Blivtech"
        val w = footerPaint.measureText(poweredBy)
        canvas.drawText(poweredBy, pageWidth - marginX - w, footerY, footerPaint)
    }

    fun save(fileName: String): File {
        finishPageFooter()
        document.finishPage(page)

        val dir = File(
            context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
            "EmpTrack/Reports"
        ).apply { if (!exists()) mkdirs() }

        val file = File(dir, fileName)
        FileOutputStream(file).use { document.writeTo(it) }
        document.close()
        return file
    }
}