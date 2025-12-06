// WorkoutQueue.kt
package com.example.zenfit

data class QueuedWorkout(
    val id: Long,
    val userId: String,
    val name: String,
    val duration: Int,
    val reps: Int,
    val sets: Int,
    val weight: Int,
    val restTime: Int,
    val timestamp: Long
)
