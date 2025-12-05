package com.example.zenfit

data class Meal(
    val id: Int = 0,
    val name: String,
    val calories: Int,
    val carbs: String = "0",
    val protein: String = "0",
    val imageUrl: String? = null
)
