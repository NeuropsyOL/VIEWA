package de.uol.neuropsy.viewa.ui.main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.google.android.material.appbar.MaterialToolbar
import de.uol.neuropsy.viewa.service.LSLService
import de.uol.neuropsy.viewa.R
import de.uol.neuropsy.viewa.ui.plot.FullScreenPlotFragment
import de.uol.neuropsy.viewa.ui.plot.LivePlotFragment

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_settings"
)

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Enable edge-to-edge so AppBarLayout can extend behind the status bar
        WindowCompat.setDecorFitsSystemWindows(window, false)
        // Both themes use a dark-blue toolbar → white status bar icons in both modes
        WindowCompat.getInsetsController(window, window.decorView)
            .isAppearanceLightStatusBars = false
        // Find the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        // Set it as the support ActionBar
        setSupportActionBar(toolbar)
        supportFragmentManager
            .addOnBackStackChangedListener {
                val current =
                    supportFragmentManager
                        .findFragmentById(R.id.fragment_container)
                when (current) {
                    is LivePlotFragment -> supportActionBar?.title = "Viewa"
                    is FullScreenPlotFragment -> true // Will set title manually
                }
            }


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