// WorkoutQueueManager.kt
package com.example.zenfit

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class WorkoutQueueManager(context: Context) {
    private val prefs = context.getSharedPreferences("WorkoutQueue", Context.MODE_PRIVATE)
    private val gson = Gson()

    fun addToQueue(workout: QueuedWorkout) {
        val queue = getQueue().toMutableList()
        queue.add(workout)
        saveQueue(queue)
    }

    fun getQueue(): List<QueuedWorkout> {
        val json = prefs.getString("queued_workouts", "[]")
        val type = object : TypeToken<List<QueuedWorkout>>() {}.type
        return gson.fromJson(json, type) ?: emptyList()
    }

    fun removeFromQueue(workoutId: Long) {
        val queue = getQueue().filter { it.id != workoutId }
        saveQueue(queue)
    }

    fun clearQueue() {
        prefs.edit().remove("queued_workouts").apply()
    }

    private fun saveQueue(queue: List<QueuedWorkout>) {
        val json = gson.toJson(queue)
        prefs.edit().putString("queued_workouts", json).apply()
    }
}
