package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class Themes : AppCompatActivity() {
    private lateinit var checkboxLight: CheckBox
    private lateinit var checkboxDark: CheckBox
    private lateinit var cardLightTheme: RelativeLayout
    private lateinit var cardDarkTheme: RelativeLayout
    private lateinit var rootLayout: RelativeLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        rootLayout = findViewById(R.id.rootLayout)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        checkboxLight = findViewById(R.id.checkboxLightTheme)
        checkboxDark = findViewById(R.id.checkboxDarkTheme)
        cardLightTheme = findViewById(R.id.cardLightTheme)
        cardDarkTheme = findViewById(R.id.cardDarkTheme)

        btnBack.setOnClickListener {
            finish()
        }

        // Load saved theme
        val isDarkMode = getThemePreference()
        checkboxLight.isChecked = !isDarkMode
        checkboxDark.isChecked = isDarkMode
        applyTheme(isDarkMode)

        // Light theme click
        cardLightTheme.setOnClickListener {
            selectLightTheme()
        }

        checkboxLight.setOnClickListener {
            selectLightTheme()
        }

        // Dark theme click
        cardDarkTheme.setOnClickListener {
            selectDarkTheme()
        }

        checkboxDark.setOnClickListener {
            selectDarkTheme()
        }
    }

    private fun selectLightTheme() {
        checkboxLight.isChecked = true
        checkboxDark.isChecked = false
        saveThemePreference(false)
        applyTheme(false)
    }

    private fun selectDarkTheme() {
        checkboxLight.isChecked = false
        checkboxDark.isChecked = true
        saveThemePreference(true)
        applyTheme(true)
    }

    private fun applyTheme(isDarkMode: Boolean) {
        if (isDarkMode) {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background)
        } else {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
        }
    }


    private fun saveThemePreference(isDarkMode: Boolean) {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("isDarkMode", isDarkMode).apply()
    }

    private fun getThemePreference(): Boolean {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        return prefs.getBoolean("isDarkMode", false)
    }
}
