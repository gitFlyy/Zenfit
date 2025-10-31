package com.example.zenfit

import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MealLogging : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mealLogAdapter: MealLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_logging)

        setupBackButton()
        setupRecyclerView()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.mealsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        val meals = listOf(
            Meal("Breakfast Bowl", 450, 52, 18),
            Meal("Grilled Chicken", 380, 12, 42),
            Meal("Pasta Salad", 520, 68, 15),
            Meal("Protein Shake", 280, 8, 35),
            Meal("Veggie Wrap", 340, 45, 12),
            Meal("Salmon Dinner", 580, 22, 48),
            Meal("Fruit Smoothie", 220, 48, 6),
            Meal("Tuna Sandwich", 410, 38, 32),
            Meal("Quinoa Bowl", 460, 58, 16),
            Meal("Steak & Potatoes", 680, 42, 52)
        )

        mealLogAdapter = MealLogAdapter(meals)
        recyclerView.adapter = mealLogAdapter
    }
}
