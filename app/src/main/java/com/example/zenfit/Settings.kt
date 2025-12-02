package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class Settings : AppCompatActivity() {
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        sessionManager = SessionManager(this)

        val backBtn = findViewById<ImageView>(R.id.backBtn)
        val accountOption = findViewById<RelativeLayout>(R.id.account)
        val changePasswordOption = findViewById<RelativeLayout>(R.id.changePasswordOption)
        val themeOption = findViewById<RelativeLayout>(R.id.themeOption)
        val logoutOption = findViewById<RelativeLayout>(R.id.logoutOption)

        backBtn.setOnClickListener {
            finish()
        }

        accountOption.setOnClickListener {
            val intent = Intent(this, Account::class.java)
            startActivity(intent)
        }

        changePasswordOption.setOnClickListener {
            val intent = Intent(this, ChangePassword::class.java)
            startActivity(intent)
        }

        themeOption.setOnClickListener {
            val intent = Intent(this, Themes::class.java)
            startActivity(intent)
        }

        logoutOption.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                performLogout()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun performLogout() {
        sessionManager.logoutUser()

        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, Login::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    private fun applyTheme() {
        try {
            val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
            val isDarkMode = prefs.getBoolean("isDarkMode", false)

            val rootLayout = findViewById<LinearLayout>(R.id.main)
            if (isDarkMode) {
                rootLayout.setBackgroundResource(R.drawable.zenfit_background)
            } else {
                rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
