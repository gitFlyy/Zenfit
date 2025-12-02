package com.example.zenfit

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import android.widget.ImageView
import android.widget.RelativeLayout

class MealIdeas : AppCompatActivity() {

    private lateinit var mealIdeasRecyclerView: RecyclerView
    private lateinit var mealIdeasAdapter: MealIdeasAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_ideas)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish()
        }

        mealIdeasRecyclerView = findViewById(R.id.mealIdeasRecyclerView)
        mealIdeasRecyclerView.layoutManager = LinearLayoutManager(this)

        val dummyMealIdeas = listOf(
            Meal("Grilled Chicken Salad", 450, 25, 35),
            Meal("Salmon with Veggies", 520, 15, 42),
            Meal("Quinoa Bowl", 380, 45, 18),
            Meal("Turkey Wrap", 410, 38, 28),
            Meal("Greek Yogurt Parfait", 290, 35, 20)
        )

        mealIdeasAdapter = MealIdeasAdapter(dummyMealIdeas)
        mealIdeasRecyclerView.adapter = mealIdeasAdapter
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<RelativeLayout>(R.id.main)
        if (isDarkMode) {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background)
        } else {
            rootLayout.setBackgroundResource(R.drawable.zenfit_background_light)
        }
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }
}
