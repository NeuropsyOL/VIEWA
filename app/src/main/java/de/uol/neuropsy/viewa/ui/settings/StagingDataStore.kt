package de.uol.neuropsy.viewa.ui.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.preference.PreferenceDataStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * A PreferenceDataStore that stages all writes in memory
 * and only persists them to the provided DataStore<Preferences>
 * when commit() is called.
 */
class StagingDataStore(
    private val store: DataStore<Preferences>
) : PreferenceDataStore() {

    // In‐memory staging buffer
    private val staging = mutableMapOf<String, Any?>()

    // ──────────────────────── READS ─────────────────────────────────

    override fun getString(key: String, defValue: String?): String? = runBlocking {
        staging[key] as String?
            ?: store.data.first()[stringPreferencesKey(key)]
            ?: defValue
    }

    override fun getBoolean(key: String, defValue: Boolean): Boolean = runBlocking {
        staging[key] as Boolean?
            ?: store.data.first()[booleanPreferencesKey(key)]
            ?: defValue
    }

    override fun getInt(key: String, defValue: Int): Int = runBlocking {
        staging[key] as Int?
            ?: store.data.first()[intPreferencesKey(key)]
            ?: defValue
    }


     //──────────────────────── WRITES ─────────────────────────────────

    override fun putString(key: String, value: String?) {
        staging[key] = value
    }

    override fun putBoolean(key: String, value: Boolean) {
        staging[key] = value
    }

    override fun putInt(key: String, value: Int) {
        staging[key] = value
    }

     //───────────────────── COMMIT STAGED CHANGES ────────────────────

    /**
     * Push all staged values into the DataStore, then clear the buffer.
     */
    fun commit() {
        if (staging.isEmpty()) return

         //Perform edits on IO dispatcher
        CoroutineScope(Dispatchers.IO).launch {
            store.edit { prefs ->
                staging.forEach { (key, value) ->
                    when (value) {
                        is Boolean -> prefs[booleanPreferencesKey(key)] = value
                        is String  -> prefs[stringPreferencesKey(key)] = value
                        is Int     -> prefs[intPreferencesKey(key)] = value
                    }
                }
            }
            staging.clear()
        }
    }
}