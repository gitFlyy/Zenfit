// WorkoutModels.kt
package com.example.zenfit

data class Exercise(
    val id: Int,
    val name: String,
    val reps: Int,
    val sets: Int,
    val weight: Int,
    val restTime: Int,
    val completedSets: Int = 0,
    val isFavorite: Boolean = false,
    val duration: Int = 0 // in minutes
)
data class Workout(
    val name: String,
    val duration: Int,
    val reps: Int,
    val sets: Int,
    val weight: Int = 150,
    val restTime: Int = 60,
    val exerciseCount: Int = 1
)