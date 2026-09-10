package com.ergovision.app.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Environment
import java.io.File

/**
 * Cross-device Office Kit Bridge utilities.
 * Powers Shared Clipboard sync (weekly summaries) and File Transfer (Room CSV exports).
 */
class OfficeKitBridge(private val context: Context) {

    fun copyToSharedClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
    }

    fun saveCsvForFileTransfer(csvContent: String): File {
        val exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
            ?: File(context.filesDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "ergovision_hazard_logs.csv")
        file.writeText(csvContent)
        return file
    }

    fun createShareIntent(csvContent: String): Intent {
        return Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "ErgoVision Hazard Compliance Report")
            putExtra(Intent.EXTRA_TEXT, csvContent)
        }
    }
}
