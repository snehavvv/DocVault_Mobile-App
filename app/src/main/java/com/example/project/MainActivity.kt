package com.example.project

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.project.core.navigation.Screen
import com.example.project.ui.MainViewModel
import com.example.project.ui.dashboard.DashboardScreen
import com.example.project.ui.notes.SecureNotesScreen
import com.example.project.ui.onboarding.OnboardingScreen
import com.example.project.ui.organizer.FileOrganizerScreen
import com.example.project.ui.pdf.ImageToPdfScreen
import com.example.project.ui.pdf.MergePdfScreen
import com.example.project.ui.pdf.PdfToImageScreen
import com.example.project.ui.scanner.ScannerScreen
import com.example.project.ui.search.SearchScreen
import com.example.project.ui.settings.SettingsScreen
import com.example.project.ui.signature.SignatureScreen
import com.example.project.ui.theme.DocVaultTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : FragmentActivity() {
    
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsState()
            
            DocVaultTheme(themeMode = themeMode) {
                val startDestination by viewModel.startDestination.collectAsState()
                val navController = rememberNavController()
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                if (startDestination == null) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        gesturesEnabled = currentRoute == Screen.Dashboard.route,
                        drawerContent = {
                            ModalDrawerSheet {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    "DocVault",
                                    modifier = Modifier.padding(16.dp),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                HorizontalDivider()
                                Spacer(Modifier.height(12.dp))
                                
                                NavigationDrawerItem(
                                    label = { Text("Dashboard") },
                                    selected = currentRoute == Screen.Dashboard.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Dashboard.route) { inclusive = true }
                                        }
                                    },
                                    icon = { Icon(Icons.Default.Home, contentDescription = null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    label = { Text("File Organizer") },
                                    selected = currentRoute == Screen.FileOrganizer.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.FileOrganizer.route)
                                    },
                                    icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                NavigationDrawerItem(
                                    label = { Text("Secure Notes") },
                                    selected = currentRoute == Screen.SecureNotes.route,
                                    onClick = {
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.SecureNotes.route)
                                    },
                                    icon = { Icon(Icons.Default.Lock, contentDescription = null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                
                                Spacer(Modifier.weight(1f))
                                NavigationDrawerItem(
                                    label = { Text("Settings") },
                                    selected = currentRoute == Screen.Settings.route,
                                    onClick = { 
                                        scope.launch { drawerState.close() }
                                        navController.navigate(Screen.Settings.route)
                                    },
                                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                                )
                                Spacer(Modifier.height(12.dp))
                            }
                        }
                    ) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            NavHost(
                                navController = navController,
                                startDestination = startDestination!!
                            ) {
                                composable(Screen.Onboarding.route) {
                                    OnboardingScreen(onFinished = {
                                        navController.navigate(Screen.Dashboard.route) {
                                            popUpTo(Screen.Onboarding.route) { inclusive = true }
                                        }
                                    })
                                }
                                composable(Screen.Dashboard.route) {
                                    DashboardScreen(
                                        onNavigate = { route ->
                                            navController.navigate(route)
                                        },
                                        onOpenDrawer = {
                                            scope.launch { drawerState.open() }
                                        }
                                    )
                                }
                                composable(Screen.Search.route) {
                                    SearchScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.Scanner.route) {
                                    ScannerScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.SignaturePad.route) {
                                    SignatureScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.SecureNotes.route) {
                                    SecureNotesScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.FileOrganizer.route) {
                                    FileOrganizerScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.ImageToPdf.route) {
                                    ImageToPdfScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.PdfToImage.route) {
                                    PdfToImageScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.MergePdf.route) {
                                    MergePdfScreen(onBack = { navController.popBackStack() })
                                }
                                composable(Screen.Settings.route) {
                                    SettingsScreen(onBack = { navController.popBackStack() })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
