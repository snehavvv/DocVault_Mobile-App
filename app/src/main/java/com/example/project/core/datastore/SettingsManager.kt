package com.example.project.core.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore("settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext private val context: Context) {
    private val settingsDataStore = context.dataStore

    val isOnboardingCompleted: Flow<Boolean> = settingsDataStore.data.map { preferences ->
        preferences[ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsDataStore.edit { preferences ->
            preferences[ONBOARDING_COMPLETED] = completed
        }
    }

    val themeMode: Flow<String> = settingsDataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        settingsDataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }

    companion object {
        private val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        private val THEME_MODE = stringPreferencesKey("theme_mode")
    }
}