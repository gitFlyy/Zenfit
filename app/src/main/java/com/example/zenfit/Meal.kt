package com.example.zenfit

data class Meal(
    val name: String,
    val calories: Int,
    val carbs: Int,
    val protein: Int,
    val imageResId: Int = R.drawable.meal_placeholder
)
