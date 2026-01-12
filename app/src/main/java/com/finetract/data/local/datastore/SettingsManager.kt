package com.finetract.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsManager @Inject constructor(@ApplicationContext context: Context) {
    private val dataStore = context.dataStore

    companion object {
        val DAILY_LIMIT = doublePreferencesKey("daily_limit")
    }

    val dailyLimit: Flow<Double> = dataStore.data.map { preferences ->
        preferences[DAILY_LIMIT] ?: 500.0 // Default limit
    }

    suspend fun setDailyLimit(limit: Double) {
        dataStore.edit { preferences ->
            preferences[DAILY_LIMIT] = limit
        }
    }
}
