package com.example.project.ui.notes

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.project.core.util.BiometricPromptUtils
import com.example.project.data.local.NoteEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SecureNotesScreen(
    onBack: () -> Unit,
    viewModel: NotesViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val notes by viewModel.allNotes.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }
    var isAuthenticated by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        BiometricPromptUtils.showBiometricPrompt(
            activity = context as FragmentActivity,
            onSuccess = { isAuthenticated = true },
            onError = { onBack() }
        )
    }

    if (!isAuthenticated) return

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Secure Notes") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Note")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(notes) { note ->
                NoteCard(note = note, onDelete = { viewModel.deleteNote(note) })
            }
        }

        if (showAddDialog) {
            AddNoteDialog(
                onDismiss = { showAddDialog = false },
                onSave = { title, content ->
                    viewModel.addNote(title, content)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun NoteCard(note: NoteEntity, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = note.content, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun AddNoteDialog(onDismiss: () -> Unit, onSave: (String, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var content by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Secure Note") },
        text = {
            Column {
                TextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                TextField(value = content, onValueChange = { content = it }, label = { Text("Content") })
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(title, content) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
