package com.example.zenfit

import android.os.Bundle
import android.widget.CheckBox
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class Themes : AppCompatActivity() {

    private lateinit var checkboxLightTheme: CheckBox
    private lateinit var checkboxDarkTheme: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_themes)

        checkboxLightTheme = findViewById(R.id.checkboxLightTheme)
        checkboxDarkTheme = findViewById(R.id.checkboxDarkTheme)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)

        checkboxLightTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxDarkTheme.isChecked = false
            }
        }

        checkboxDarkTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                checkboxLightTheme.isChecked = false
            }
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
