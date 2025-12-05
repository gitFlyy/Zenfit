package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.ScrollView
import androidx.appcompat.app.AppCompatActivity

class CaloriesActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calories)

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        btnBack.setOnClickListener {
            finish()
        }

        applyTheme()
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<ScrollView>(R.id.main)
        if (isDarkMode) {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background)
        } else {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
