package com.example.project.ui.signature

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.project.core.util.FileUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignatureScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var currentPath by remember { mutableStateOf<androidx.compose.ui.graphics.Path?>(null) }
    val paths = remember { mutableStateListOf<androidx.compose.ui.graphics.Path>() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Signature Pad") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { paths.clear() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                    IconButton(onClick = {
                        // Logic to save as PNG
                        val bitmap = Bitmap.createBitmap(1080, 720, Bitmap.Config.ARGB_8888)
                        val canvas = android.graphics.Canvas(bitmap)
                        canvas.drawColor(Color.WHITE)
                        val paint = android.graphics.Paint().apply {
                            color = Color.BLACK
                            strokeWidth = 10f
                            style = android.graphics.Paint.Style.STROKE
                            isAntiAlias = true
                        }
                        paths.forEach { path ->
                            canvas.drawPath(path.asAndroidPath(), paint)
                        }
                        FileUtils.saveBitmapToFile(context, bitmap, "signature_${System.currentTimeMillis()}")
                        onBack()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(
                "Sign inside the box",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(16.dp))
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDragStart = { offset ->
                                currentPath = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(offset.x, offset.y)
                                }
                                currentPath?.let { paths.add(it) }
                            },
                            onDrag = { change, dragAmount ->
                                change.consume()
                                currentPath?.lineTo(change.position.x, change.position.y)
                                // Trigger recomposition
                                val last = paths.lastOrNull()
                                if (last != null) {
                                    paths[paths.size - 1] = last
                                }
                            }
                        )
                    }
            ) {
                androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                    paths.forEach { path ->
                        drawPath(
                            path = path,
                            color = androidx.compose.ui.graphics.Color.Black,
                            style = Stroke(width = 5.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}
