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
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.project.core.util.FileUtils
import com.example.project.core.util.PdfUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageToPdfScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedImages by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents(),
        onResult = { uris ->
            selectedImages = uris
        }
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Image to PDF") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            if (selectedImages.isNotEmpty() && !isProcessing) {
                ExtendedFloatingActionButton(
                    onClick = {
                        isProcessing = true
                        scope.launch {
                            val outputDir = FileUtils.getOutputDirectory(context)
                            val fileName = "converted_${System.currentTimeMillis()}"
                            val result = withContext(Dispatchers.IO) {
                                PdfUtils.convertImageUrisToPdf(
                                    context = context,
                                    imageUris = selectedImages,
                                    outputFileName = fileName,
                                    outputDir = outputDir
                                )
                            }
                            isProcessing = false
                            if (result != null) {
                                Toast.makeText(context, "PDF saved to ${result.absolutePath}", Toast.LENGTH_LONG).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed to convert images", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    icon = { Icon(Icons.Default.PictureAsPdf, null) },
                    text = { Text("Convert to PDF") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = !isProcessing
                ) {
                    Icon(Icons.Default.Add, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Images")
                }

                if (selectedImages.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No images selected", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f).fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages) { uri ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                            ) {
                                AsyncImage(
                                    model = uri,
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
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
                        Text("Converting to PDF...")
                    }
                }
            }
        }
    }
}
