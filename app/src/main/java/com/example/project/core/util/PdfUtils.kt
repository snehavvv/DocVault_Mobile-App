package com.example.project.core.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import java.io.File
import java.io.FileOutputStream

object PdfUtils {
    fun convertImageUrisToPdf(context: Context, imageUris: List<Uri>, outputFileName: String, outputDir: File): File? {
        val pdfDocument = PdfDocument()
        
        imageUris.forEachIndexed { index, uri ->
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return@forEachIndexed
                
                // Keep original bitmap size or scale if needed
                val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, index + 1).create()
                val page = pdfDocument.startPage(pageInfo)
                val canvas = page.canvas
                canvas.drawBitmap(bitmap, 0f, 0f, null)
                pdfDocument.finishPage(page)
                
                bitmap.recycle()
                inputStream?.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return try {
            val file = File(outputDir, "$outputFileName.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun convertPdfToImages(context: Context, pdfUri: Uri, outputDir: File): List<File> {
        val savedFiles = mutableListOf<File>()
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(pdfUri, "r") ?: return emptyList()
            val pdfRenderer = PdfRenderer(parcelFileDescriptor)
            
            val baseFileName = "pdf_page_${System.currentTimeMillis()}"
            
            for (i in 0 until pdfRenderer.pageCount) {
                val page = pdfRenderer.openPage(i)
                
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                canvas.drawColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                
                val file = File(outputDir, "${baseFileName}_$i.png")
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                out.flush()
                out.close()
                
                savedFiles.add(file)
                page.close()
            }
            
            pdfRenderer.close()
            parcelFileDescriptor.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return savedFiles
    }

    fun mergePdfUris(context: Context, pdfUris: List<Uri>, outputFileName: String, outputDir: File): File? {
        val pdfDocument = PdfDocument()
        var pageCounter = 0

        pdfUris.forEach { uri ->
            try {
                val pfd = context.contentResolver.openFileDescriptor(uri, "r") ?: return@forEach
                val renderer = PdfRenderer(pfd)
                
                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    
                    // Create a bitmap to capture the page content
                    val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    val canvas = Canvas(bitmap)
                    canvas.drawColor(Color.WHITE)
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    
                    val pageInfo = PdfDocument.PageInfo.Builder(page.width, page.height, pageCounter + 1).create()
                    val pdfPage = pdfDocument.startPage(pageInfo)
                    val pdfCanvas = pdfPage.canvas
                    pdfCanvas.drawBitmap(bitmap, 0f, 0f, null)
                    pdfDocument.finishPage(pdfPage)
                    
                    bitmap.recycle()
                    page.close()
                    pageCounter++
                }
                renderer.close()
                pfd.close()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        if (pageCounter == 0) {
            pdfDocument.close()
            return null
        }

        return try {
            val file = File(outputDir, "$outputFileName.pdf")
            val outputStream = FileOutputStream(file)
            pdfDocument.writeTo(outputStream)
            pdfDocument.close()
            outputStream.close()
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
