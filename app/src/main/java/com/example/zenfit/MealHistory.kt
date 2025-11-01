package com.example.zenfit

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MealHistory : AppCompatActivity() {

    private lateinit var datesRecyclerView: RecyclerView
    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private lateinit var mealHistoryAdapter: MealHistoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_history)

        datesRecyclerView = findViewById(R.id.datesRecyclerView)
        mealsRecyclerView = findViewById(R.id.mealsRecyclerView)

        setupDatesRecyclerView()
        setupMealsRecyclerView()
    }

    private fun setupDatesRecyclerView() {
        val dates = listOf(
            DateItem("Aug", "10", true),
            DateItem("Aug", "11"),
            DateItem("Aug", "12"),
            DateItem("Aug", "13"),
            DateItem("Aug", "14"),
            DateItem("Aug", "15"),
            DateItem("Aug", "16")
        )

        dateAdapter = DateAdapter(dates) { position ->
            // Handle date selection - could load meals for selected date here
        }

        datesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MealHistory, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }
    }

    private fun setupMealsRecyclerView() {
        val meals = listOf(
            Meal("Salad with eggs", 284, 12, 22, 45),
            Meal("Pancakes", 296, 9, 12, 42),
            Meal("Protein Shake", 264, 18, 32, 12),
            Meal("Avocado", 284, 18, 33, 19),
            Meal("Chicken Breast", 165, 31, 3, 4),
            Meal("Brown Rice", 112, 2, 0, 24)
        )

        mealHistoryAdapter = MealHistoryAdapter(meals)

        mealsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MealHistory)
            adapter = mealHistoryAdapter
        }
    }

}
