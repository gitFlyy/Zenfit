package com.example.zenfit

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class Settings : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_settings)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val settingsBtn = findViewById<RelativeLayout>(R.id.account)
        settingsBtn.setOnClickListener {
            val intent = Intent(this, Account::class.java)
            startActivity(intent)
        }
        val changePasswordBtn = findViewById<RelativeLayout>(R.id.changePasswordOption)
        changePasswordBtn.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }
        val themesBtn = findViewById<RelativeLayout>(R.id.themeOption)
        themesBtn.setOnClickListener {
            val intent = Intent(this, Themes::class.java)
            startActivity(intent)
        }

    }
}