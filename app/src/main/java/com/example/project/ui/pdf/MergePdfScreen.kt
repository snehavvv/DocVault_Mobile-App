package com.example.project.ui.pdf

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MergeType
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
fun MergePdfScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedPdfs by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments(),
        onResult = { uris ->
            if (uris.isNotEmpty()) {
                selectedPdfs = uris
            }
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Merge PDFs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedPdfs.size > 1 && !isProcessing) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val outputDir = FileUtils.getOutputDirectory(context)
                            val fileName = "merged_${System.currentTimeMillis()}"
                            val result = withContext(Dispatchers.IO) {
                                PdfUtils.mergePdfUris(
                                    context = context,
                                    pdfUris = selectedPdfs,
                                    outputFileName = fileName,
                                    outputDir = outputDir
                                )
                            }
                            isProcessing = false
                            if (result != null) {
                                Toast.makeText(context, "PDFs merged successfully!", Toast.LENGTH_LONG).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed to merge PDFs", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.MergeType, null) },
                    text = { Text("Merge ${selectedPdfs.size} Files") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { launcher.launch(arrayOf("application/pdf")) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select PDF Files")
                }

                if (selectedPdfs.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("Select at least 2 PDF files to merge", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedPdfs) { uri ->
                            Card(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Default.MergeType, null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(
                                        text = uri.path?.substringAfterLast('/') ?: "Unknown File",
                                        modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                }
                            }
                        }
                    }
                }
            }

            if (isProcessing) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Merging PDFs...")
                    }
                }
            }
        }
    }
}
