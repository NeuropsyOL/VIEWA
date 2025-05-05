package de.uol.viewa

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.appbar.MaterialToolbar
import de.uol.neuropsy.viewa.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 1) Find the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        // 2) Set it as the support ActionBar
        setSupportActionBar(toolbar)

        // 3) Hook it up with NavController so titles update automatically
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment)
                    as NavHostFragment
        val navController = navHost.navController
        setupActionBarWithNavController(navController)
    }

    // 4) Enable the Up button
    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment)
            .navigateUp() || super.onSupportNavigateUp()
    }
}