package com.example.project.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.project.core.datastore.SettingsManager
import com.example.project.core.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsManager: SettingsManager
) : ViewModel() {

    private val _startDestination = MutableStateFlow<String?>(null)
    val startDestination: StateFlow<String?> = _startDestination

    val themeMode: StateFlow<String> = settingsManager.themeMode
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "system")

    init {
        viewModelScope.launch {
            val onboardingCompleted = settingsManager.isOnboardingCompleted.first()
            _startDestination.value = if (onboardingCompleted) Screen.Dashboard.route else Screen.Onboarding.route
        }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            settingsManager.setThemeMode(mode)
        }
    }
}