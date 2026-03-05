package com.example.project.core.util

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Environment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object FileUtils {
    fun saveBitmapToFile(context: Context, bitmap: Bitmap, fileName: String): File? {
        val storageDir = getOutputDirectory(context)
        return try {
            val file = File(storageDir, "$fileName.png")
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun getOutputDirectory(context: Context): File {
        val mediaDir = context.getExternalFilesDir(null)?.let {
            File(it, "DocVault").apply { mkdirs() }
        }
        return if (mediaDir != null && mediaDir.exists()) mediaDir else context.filesDir
    }

    fun createFile(baseFolder: File, prefix: String, extension: String): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        return File(baseFolder, "$prefix$timeStamp$extension")
    }
    
    fun getAllFiles(context: Context, extensions: List<String>): List<File> {
        val directory = getOutputDirectory(context)
        return directory.listFiles { file -> 
            file.isFile && extensions.any { ext -> file.extension.equals(ext, ignoreCase = true) }
        }?.toList()?.sortedByDescending { it.lastModified() } ?: emptyList()
    }
}
