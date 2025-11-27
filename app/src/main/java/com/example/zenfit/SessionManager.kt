package com.example.zenfit

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {
    val prefs: SharedPreferences = context.getSharedPreferences("ZenfitSession", Context.MODE_PRIVATE)

    companion object {
        const val KEY_IS_LOGGED_IN = "isLoggedIn"
        const val KEY_USER_ID = "userId"
        const val KEY_USERNAME = "username"
        const val KEY_EMAIL = "email"
        const val KEY_FULL_NAME = "fullName"
    }

    fun createLoginSession(userId: String, username: String, email: String, fullName: String?) {
        val editor = prefs.edit()
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.putString(KEY_USER_ID, userId)
        editor.putString(KEY_USERNAME, username)
        editor.putString(KEY_EMAIL, email)
        editor.putString(KEY_FULL_NAME, fullName)
        editor.apply()
    }

    fun isLoggedIn(): Boolean {
        return prefs.getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun logoutUser() {
        prefs.edit().clear().apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }
}
