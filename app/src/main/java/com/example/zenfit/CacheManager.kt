// CacheManager.kt
package com.example.zenfit

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CacheManager(context: Context) {
    private val prefs = context.getSharedPreferences("WorkoutCache", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun cacheWorkouts(workouts: List<Workout>) {
        val json = gson.toJson(workouts)
        prefs.edit().putString("cached_workouts", json).apply()
    }

    fun getCachedWorkouts(): List<Workout> {
        val json = prefs.getString("cached_workouts", "[]")
        val type = object : TypeToken<List<Workout>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun cacheWorkoutHistory(history: List<WorkoutHistoryItem>) {
        val json = gson.toJson(history)
        prefs.edit().putString("cached_history", json).apply()
    }

    fun getCachedWorkoutHistory(): List<WorkoutHistoryItem> {
        val json = prefs.getString("cached_history", "[]")
        val type = object : TypeToken<List<WorkoutHistoryItem>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun clearCache() {
        prefs.edit().clear().apply()
    }
}
