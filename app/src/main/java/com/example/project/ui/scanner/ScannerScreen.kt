package com.example.project.ui.scanner

import android.Manifest
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.project.core.util.FileUtils
import com.example.project.core.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun ScannerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var hasCameraPermission by remember { mutableStateOf(false) }
    
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )
    
    LaunchedEffect(Unit) {
        launcher.launch(Manifest.permission.CAMERA)
    }

    if (hasCameraPermission) {
        ScannerCamera(onBack)
    } else {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.Camera, contentDescription = null, modifier = Modifier.size(64.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Camera Permission Required")
                Button(onClick = { launcher.launch(Manifest.permission.CAMERA) }) {
                    Text("Grant Permission")
                }
            }
        }
    }
}

@Composable
fun ScannerCamera(onBack: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val capturedImages = remember { mutableStateListOf<Uri>() }
    
    var isProcessing by remember { mutableStateOf(false) }
    var isCapturing by remember { mutableStateOf(false) }
    var isCameraReady by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, 
                    CameraSelector.DEFAULT_BACK_CAMERA, 
                    preview, 
                    imageCapture
                )
                isCameraReady = true
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Camera binding failed", e)
                isCameraReady = false
            }
        }, ContextCompat.getMainExecutor(context))

        onDispose {
            try {
                val cameraProvider = ProcessCameraProvider.getInstance(context).get()
                cameraProvider.unbindAll()
            } catch (e: Exception) {
                Log.e("ScannerScreen", "Camera unbind failed", e)
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .statusBarsPadding(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                colors = IconButtonDefaults.iconButtonColors(containerColor = Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
            }
            
            if (capturedImages.isNotEmpty()) {
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        "${capturedImages.size} pages", 
                        color = Color.White, 
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }

        // Captured Images Preview
        if (capturedImages.isNotEmpty()) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(bottom = 120.dp, start = 16.dp)
                    .fillMaxWidth()
            ) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(capturedImages) { uri ->
                        AsyncImage(
                            model = uri,
                            contentDescription = null,
                            modifier = Modifier
                                .size(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(2.dp, Color.White, RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        // Bottom Controls
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(bottom = 40.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clear all
                if (capturedImages.isNotEmpty() && !isCapturing) {
                    IconButton(
                        onClick = { capturedImages.clear() },
                        modifier = Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }

                // Capture Button
                FloatingActionButton(
                    onClick = {
                        if (isCapturing || !isCameraReady) return@FloatingActionButton
                        
                        try {
                            isCapturing = true
                            val outputDir = FileUtils.getOutputDirectory(context)
                            val outputFile = FileUtils.createFile(outputDir, "scan_", ".jpg")
                            val outputOptions = ImageCapture.OutputFileOptions.Builder(outputFile).build()

                            imageCapture.takePicture(
                                outputOptions,
                                ContextCompat.getMainExecutor(context),
                                object : ImageCapture.OnImageSavedCallback {
                                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                                        isCapturing = false
                                        val savedUri = output.savedUri ?: Uri.fromFile(outputFile)
                                        capturedImages.add(savedUri)
                                    }
                                    override fun onError(exc: ImageCaptureException) {
                                        isCapturing = false
                                        Log.e("ScannerScreen", "Capture failed: ${exc.message}", exc)
                                        Toast.makeText(context, "Failed to capture", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            )
                        } catch (e: Exception) {
                            isCapturing = false
                            Log.e("ScannerScreen", "Take picture error", e)
                            Toast.makeText(context, "Error starting capture", Toast.LENGTH_SHORT).show()
                        }
                    },
                    containerColor = if (isCapturing || !isCameraReady) Color.Gray else Color.White,
                    contentColor = Color.Black,
                    shape = CircleShape,
                    modifier = Modifier.size(72.dp)
                ) {
                    if (isCapturing) {
                        CircularProgressIndicator(modifier = Modifier.size(32.dp), color = Color.Black)
                    } else {
                        Icon(Icons.Default.Camera, contentDescription = "Capture", modifier = Modifier.size(36.dp))
                    }
                }

                // Done Button
                if (capturedImages.isNotEmpty() && !isCapturing) {
                    IconButton(
                        onClick = { showOptions = true },
                        modifier = Modifier.background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = "Done", tint = Color.White)
                    }
                } else {
                    Spacer(modifier = Modifier.size(48.dp))
                }
            }
        }

        if (showOptions) {
            AlertDialog(
                onDismissRequest = { showOptions = false },
                title = { Text("Save Scan As") },
                text = { Text("Choose your preferred format for the ${capturedImages.size} captured pages.") },
                confirmButton = {
                    Button(onClick = {
                        showOptions = false
                        isProcessing = true
                        scope.launch {
                            try {
                                val outputDir = FileUtils.getOutputDirectory(context)
                                val fileName = "scan_${System.currentTimeMillis()}"
                                val result = withContext(Dispatchers.IO) {
                                    PdfUtils.convertImageUrisToPdf(
                                        context, 
                                        capturedImages, 
                                        fileName, 
                                        outputDir
                                    )
                                }
                                isProcessing = false
                                if (result != null) {
                                    Toast.makeText(context, "Saved as PDF", Toast.LENGTH_SHORT).show()
                                    onBack()
                                } else {
                                    Toast.makeText(context, "Failed to save PDF", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                isProcessing = false
                                Toast.makeText(context, "An error occurred while saving", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("PDF Document")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showOptions = false
                        Toast.makeText(context, "Saved as Individual Images", Toast.LENGTH_SHORT).show()
                        onBack()
                    }) {
                        Text("Individual Images")
                    }
                }
            )
        }

        if (isProcessing) {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = Color.Black.copy(alpha = 0.5f)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color.White)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Saving PDF...", color = Color.White)
                    }
                }
            }
        }
    }
}
