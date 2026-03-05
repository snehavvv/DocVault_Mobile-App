package com.example.project.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.project.core.navigation.Screen
import com.example.project.core.util.FileUtils
import com.example.project.ui.theme.LocalSpacing
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(onNavigate: (String) -> Unit, onOpenDrawer: () -> Unit) {
    val spacing = LocalSpacing.current
    val context = LocalContext.current
    
    val recentFiles = remember {
        FileUtils.getAllFiles(context, listOf("pdf", "png", "jpg")).take(5)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("DocVault", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Search.route) }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(bottom = spacing.large)
        ) {
            // Quick Actions Section
            item {
                Text(
                    "Quick Actions",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = spacing.medium, vertical = spacing.small)
                )
                
                val cardColor = MaterialTheme.colorScheme.surfaceVariant
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    QuickActionCard(
                        title = "Scan",
                        icon = Icons.Rounded.Camera,
                        color = cardColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.Scanner.route) }
                    )
                    QuickActionCard(
                        title = "Import",
                        icon = Icons.Rounded.PictureAsPdf,
                        color = cardColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.PdfToImage.route) }
                    )
                }
                
                Spacer(modifier = Modifier.height(spacing.medium))
                
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spacing.medium),
                    horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                ) {
                    QuickActionCard(
                        title = "Merge PDF",
                        icon = Icons.Rounded.MergeType,
                        color = cardColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.MergePdf.route) }
                    )
                    QuickActionCard(
                        title = "To PDF",
                        icon = Icons.Rounded.PictureAsPdf,
                        color = cardColor,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigate(Screen.ImageToPdf.route) }
                    )
                }
            }

            // Recent Documents Section
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = spacing.medium, end = spacing.medium, top = spacing.large, bottom = spacing.small),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Recent Documents",
                        style = MaterialTheme.typography.titleMedium
                    )
                    TextButton(onClick = { onNavigate(Screen.FileOrganizer.route) }) {
                        Text("See All")
                    }
                }
                
                if (recentFiles.isEmpty()) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(spacing.medium),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Box(modifier = Modifier.padding(spacing.large).fillMaxWidth(), contentAlignment = Alignment.Center) {
                            Text("No recent documents", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = spacing.medium),
                        horizontalArrangement = Arrangement.spacedBy(spacing.medium)
                    ) {
                        items(recentFiles) { file ->
                            RecentDocItem(file)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionCard(
    title: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(100.dp),
        shape = MaterialTheme.shapes.medium,
        color = color
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = null, 
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun RecentDocItem(file: File) {
    Card(
        modifier = Modifier.width(160.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                // Placeholder for actual PDF/Image thumbnail
                Icon(
                    imageVector = if (file.extension == "pdf") Icons.Rounded.PictureAsPdf else Icons.Rounded.Image,
                    contentDescription = null,
                    modifier = Modifier.align(Alignment.Center).size(40.dp),
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                )
            }
            Column(modifier = Modifier.padding(8.dp)) {
                Text(
                    file.name,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1
                )
                Text(
                    "Yesterday", // TODO: Real date formatting
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
