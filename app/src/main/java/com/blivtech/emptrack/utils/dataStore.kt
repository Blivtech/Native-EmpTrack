package com.blivtech.emptrack.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// ✅ DataStore instance
val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "emptrack_prefs")

@Singleton
class PreferenceManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val KEY_IS_LOGGED_IN  = booleanPreferencesKey("is_logged_in")
        val KEY_TOKEN         = stringPreferencesKey("token")
        val KEY_BT_CODE       = stringPreferencesKey("bt_code")
        val KEY_USER_NAME     = stringPreferencesKey("user_name")
        val KEY_PHONE         = stringPreferencesKey("phone")
        val KEY_USER_TYPE     = stringPreferencesKey("user_type")
        val KEY_IS_SYNCED     = booleanPreferencesKey("is_synced")  // ✅ Track sync
    }

    // ✅ Save login data after successful login
    suspend fun saveLoginData(
        token: String,
        btCode: String,
        userName: String,
        phone: String,
        userType: Int
    ) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_LOGGED_IN] = true
            prefs[KEY_TOKEN]        = token
            prefs[KEY_BT_CODE]      = btCode
            prefs[KEY_USER_NAME]    = userName
            prefs[KEY_PHONE]        = phone
            prefs[KEY_USER_TYPE]    = userType.toString()
        }
    }

    // ✅ Mark sync as done — called after first sync
    suspend fun markSynced() {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_SYNCED] = true
        }
    }

    // ✅ Clear all on logout
    suspend fun clearAll() {
        context.dataStore.edit { prefs ->
            prefs.clear()
        }
    }

    // ✅ Flows to read values
    val isLoggedIn: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_IS_LOGGED_IN] ?: false }

    val isSynced: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_IS_SYNCED] ?: false }

    val token: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_TOKEN] ?: "" }

    val btCode: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_BT_CODE] ?: "" }

    val userName: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_NAME] ?: "" }

    val userType: Flow<String> = context.dataStore.data
        .map { prefs -> prefs[KEY_USER_TYPE] ?: "1" }
}