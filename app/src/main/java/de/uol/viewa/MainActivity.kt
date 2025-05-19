package de.uol.viewa

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import de.uol.neuropsy.viewa.service.LSLService
import de.uol.neuropsy.viewa.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Find the toolbar
        val toolbar: MaterialToolbar = findViewById(R.id.toolbar)
        // Set it as the support ActionBar
        setSupportActionBar(toolbar)

        startService(Intent(this, LSLService::class.java))
    }
}