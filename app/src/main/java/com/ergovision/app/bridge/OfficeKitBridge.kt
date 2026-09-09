package com.ergovision.app.bridge

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
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
        val exportDir = File(context.filesDir, "exports")
        if (!exportDir.exists()) exportDir.mkdirs()

        val file = File(exportDir, "ergovision_hazard_logs.csv")
        file.writeText(csvContent)
        return file
    }
}
