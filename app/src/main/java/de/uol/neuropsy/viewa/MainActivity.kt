package de.uol.neuropsy.viewa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
//import androidx.datastore.core.DataStore
//import androidx.datastore.preferences.core.Preferences
//import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.appbar.MaterialToolbar
import de.uol.neuropsy.viewa.ui.plot.LivePlotFragment

//val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
//    name = "user_settings"
//)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Find the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        // Set it as the support ActionBar
        setSupportActionBar(toolbar)
        startService(Intent(this, LSLService::class.java))

        // Only add the fragment if this is first creation
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(
                    R.id.fragment_container,
                    LivePlotFragment()        // ← your start fragment class
                )
                .commit()
        }
    }
}