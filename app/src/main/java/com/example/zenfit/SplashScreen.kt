package com.example.zenfit

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SplashScreen : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splashscreen)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)

        val loggedInStatusText = findViewById<TextView>(R.id.loggedInStatusText)
        val logoutButton = findViewById<Button>(R.id.LogoutButton)

        // Show/hide logout button and status based on login state
        if (sessionManager.isLoggedIn()) {
            val username = sessionManager.prefs.getString(SessionManager.KEY_USERNAME, "User")
            loggedInStatusText.text = "Currently logged in as: $username"
            loggedInStatusText.visibility = TextView.VISIBLE
            logoutButton.visibility = Button.VISIBLE
        } else {
            loggedInStatusText.visibility = TextView.GONE
            logoutButton.visibility = Button.GONE
        }

        val getStartedButton = findViewById<Button>(R.id.getStartedButton)
        getStartedButton.setOnClickListener {
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
        }

        logoutButton.setOnClickListener {
            sessionManager.logoutUser()
            val intent = Intent(this, Login::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
