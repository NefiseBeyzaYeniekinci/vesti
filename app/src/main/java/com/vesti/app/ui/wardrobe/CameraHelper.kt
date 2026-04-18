package com.vesti.app.ui.wardrobe

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CameraHelper {
    
    fun createTempImageFile(context: Context): Pair<File, Uri> {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        
        // Define directory
        val storageDir: File = File(context.cacheDir, "camera_photos")
        if (!storageDir.exists()) {
            storageDir.mkdirs()
        }
        
        // Create actual file
        val file = File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        )
        
        // Return file and provider URI (matching authorities in Manifest)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        
        return Pair(file, uri)
    }
}
