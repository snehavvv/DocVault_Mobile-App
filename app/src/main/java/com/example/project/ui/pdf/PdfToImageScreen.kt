package com.example.project.ui.pdf

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Collections
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.project.core.util.FileUtils
import com.example.project.core.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PdfToImageScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            selectedPdfUri = uri
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PDF to Image") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
                if (selectedPdfUri == null) {
                    Icon(
                        Icons.Default.PictureAsPdf,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Select a PDF to extract images",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = { launcher.launch("application/pdf") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Select PDF")
                    }
                } else {
                    Icon(
                        Icons.Default.Collections,
                        contentDescription = null,
                        modifier = Modifier.size(100.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "PDF Selected",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        selectedPdfUri?.path ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    if (isProcessing) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Extracting images...")
                    } else {
                        Button(
                            onClick = {
                                isProcessing = true
                                scope.launch {
                                    val outputDir = FileUtils.getOutputDirectory(context)
                                    val result = withContext(Dispatchers.IO) {
                                        PdfUtils.convertPdfToImages(context, selectedPdfUri!!, outputDir)
                                    }
                                    isProcessing = false
                                    if (result.isNotEmpty()) {
                                        Toast.makeText(context, "Extracted ${result.size} images to File Organizer", Toast.LENGTH_LONG).show()
                                        onBack()
                                    } else {
                                        Toast.makeText(context, "Failed to extract images", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Convert to Images")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { selectedPdfUri = null }) {
                            Text("Cancel")
                        }
                    }
                }
            }
        }
    }
}
