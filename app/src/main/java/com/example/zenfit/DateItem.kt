package com.example.zenfit

data class DateItem(
    val month: String,
    val day: String,
    val year: String,
    val fullDate: String, // Format: YYYY-MM-DD for database queries
    val isSelected: Boolean = false
)
