package com.example.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.model.LiveStockItem
import com.example.data.model.Product
import com.example.data.model.StockInTransaction
import com.example.data.model.StockOutTransaction
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

object ExcelExporter {

    private const val COMPANY_NAME = "METINEX ALUMINIUM"
    private const val AUTHORITY = "com.aistudio.metinexstock.mxalm.fileprovider"

    private fun getReportsDir(context: Context): File {
        val dir = File(context.cacheDir, "reports")
        if (!dir.exists()) {
            dir.mkdirs()
        }
        return dir
    }

    private fun shareFile(context: Context, file: File, title: String) {
        val uri = FileProvider.getUriForFile(context, AUTHORITY, file)
        val sendIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/csv"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(sendIntent, "Export $title to Excel / Sheets")
        chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(chooser)
    }

    private fun escapeCsv(value: Any?): String {
        if (value == null) return ""
        val str = value.toString().replace("\"", "\"\"")
        return if (str.contains(",") || str.contains("\"") || str.contains("\n")) {
            "\"$str\""
        } else {
            str
        }
    }

    fun exportStockIn(context: Context, items: List<StockInTransaction>): File {
        val file = File(getReportsDir(context), "METINEX_Stock_In_Report_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { fos ->
            // Write UTF-8 BOM so Microsoft Excel automatically recognizes UTF-8 encoding
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("$COMPANY_NAME - INVENTORY MANAGEMENT SYSTEM\n")
                writer.append("STOCK IN TRANSACTION REPORT\n")
                writer.append("Generated On: ${System.currentTimeMillis()}\n")
                writer.append("\n")

                // Headers
                val headers = listOf(
                    "SL NO", "DATE", "PRODUCT NAME", "HSM CODE", "FIRM NAME",
                    "PREVIOUS STOCK", "STOCK IN", "UNIT", "RATE", "TOTAL AMOUNT", "NEW STOCK"
                )
                writer.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

                var totalQty = 0.0
                var totalAmount = 0.0

                items.forEachIndexed { index, item ->
                    totalQty += item.quantity
                    totalAmount += item.totalAmount
                    val row = listOf(
                        (index + 1).toString(),
                        item.date,
                        item.productName,
                        item.hsmCode,
                        item.firmName,
                        String.format("%.2f", item.previousStock),
                        String.format("%.2f", item.quantity),
                        item.unit,
                        String.format("%.2f", item.rate),
                        String.format("%.2f", item.totalAmount),
                        String.format("%.2f", item.newStock)
                    )
                    writer.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
                }

                // Summary Row
                writer.append("\n")
                val summaryRow = listOf(
                    "TOTAL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    String.format("%.2f", totalQty),
                    "",
                    "",
                    String.format("%.2f", totalAmount),
                    ""
                )
                writer.append(summaryRow.joinToString(",") { escapeCsv(it) }).append("\n")
                writer.flush()
            }
        }
        shareFile(context, file, "Stock In Report")
        return file
    }

    fun exportStockOut(context: Context, items: List<StockOutTransaction>): File {
        val file = File(getReportsDir(context), "METINEX_Stock_Out_Report_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { fos ->
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("$COMPANY_NAME - INVENTORY MANAGEMENT SYSTEM\n")
                writer.append("STOCK OUT TRANSACTION REPORT\n")
                writer.append("Generated On: ${System.currentTimeMillis()}\n")
                writer.append("\n")

                val headers = listOf(
                    "SL NO", "DATE", "PARTY NAME", "PRODUCT NAME", "HSM CODE",
                    "FIRM NAME", "PREVIOUS STOCK", "STOCK OUT", "UNIT", "RATE", "TOTAL AMOUNT", "REMAINING STOCK"
                )
                writer.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

                var totalQty = 0.0
                var totalAmount = 0.0

                items.forEachIndexed { index, item ->
                    totalQty += item.quantity
                    totalAmount += item.totalAmount
                    val row = listOf(
                        (index + 1).toString(),
                        item.date,
                        item.partyName,
                        item.productName,
                        item.hsmCode,
                        item.firmName,
                        String.format("%.2f", item.previousStock),
                        String.format("%.2f", item.quantity),
                        item.unit,
                        String.format("%.2f", item.rate),
                        String.format("%.2f", item.totalAmount),
                        String.format("%.2f", item.remainingStock)
                    )
                    writer.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
                }

                writer.append("\n")
                val summaryRow = listOf(
                    "TOTAL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    String.format("%.2f", totalQty),
                    "",
                    "",
                    String.format("%.2f", totalAmount),
                    ""
                )
                writer.append(summaryRow.joinToString(",") { escapeCsv(it) }).append("\n")
                writer.flush()
            }
        }
        shareFile(context, file, "Stock Out Report")
        return file
    }

    fun exportLiveStock(context: Context, items: List<LiveStockItem>): File {
        val file = File(getReportsDir(context), "METINEX_Live_Stock_Report_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { fos ->
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("$COMPANY_NAME - INVENTORY MANAGEMENT SYSTEM\n")
                writer.append("REAL-TIME LIVE STOCK & INVENTORY VALUATION REPORT\n")
                writer.append("\n")

                val headers = listOf(
                    "SL NO", "PRODUCT NAME", "HSM CODE", "CATEGORY", "UNIT",
                    "OPENING STOCK", "TOTAL STOCK IN", "TOTAL STOCK OUT",
                    "AVAILABLE STOCK", "RATE PER UNIT", "TOTAL INVENTORY VALUE"
                )
                writer.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

                var totalAvail = 0.0
                var totalValue = 0.0

                items.forEachIndexed { index, item ->
                    totalAvail += item.availableStock
                    totalValue += item.totalInventoryValue
                    val row = listOf(
                        (index + 1).toString(),
                        item.productName,
                        item.hsmCode,
                        item.category,
                        item.unit,
                        String.format("%.2f", item.openingStock),
                        String.format("%.2f", item.totalStockIn),
                        String.format("%.2f", item.totalStockOut),
                        String.format("%.2f", item.availableStock),
                        String.format("%.2f", item.ratePerUnit),
                        String.format("%.2f", item.totalInventoryValue)
                    )
                    writer.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
                }

                writer.append("\n")
                val summaryRow = listOf(
                    "TOTAL",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    "",
                    String.format("%.2f", totalAvail),
                    "",
                    String.format("%.2f", totalValue)
                )
                writer.append(summaryRow.joinToString(",") { escapeCsv(it) }).append("\n")
                writer.flush()
            }
        }
        shareFile(context, file, "Live Stock Valuation Report")
        return file
    }

    fun exportProducts(context: Context, products: List<Product>): File {
        val file = File(getReportsDir(context), "METINEX_Products_Catalog_${System.currentTimeMillis()}.csv")
        FileOutputStream(file).use { fos ->
            fos.write(0xEF)
            fos.write(0xBB)
            fos.write(0xBF)
            OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                writer.append("$COMPANY_NAME - PRODUCT CATALOG\n\n")
                val headers = listOf("SL NO", "PRODUCT ID", "PRODUCT NAME", "HSM CODE", "CATEGORY", "UNIT", "RATE PER UNIT", "OPENING STOCK", "CURRENT STOCK", "MIN STOCK ALERT")
                writer.append(headers.joinToString(",") { escapeCsv(it) }).append("\n")

                products.forEachIndexed { index, p ->
                    val row = listOf(
                        (index + 1).toString(),
                        p.productId,
                        p.productName,
                        p.hsmCode,
                        p.category,
                        p.unit,
                        String.format("%.2f", p.ratePerUnit),
                        String.format("%.2f", p.openingStock),
                        String.format("%.2f", p.currentStock),
                        String.format("%.2f", p.minimumStock)
                    )
                    writer.append(row.joinToString(",") { escapeCsv(it) }).append("\n")
                }
                writer.flush()
            }
        }
        shareFile(context, file, "Products Catalog")
        return file
    }
}
