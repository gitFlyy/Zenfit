package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MealHistory : AppCompatActivity() {

    private lateinit var datesRecyclerView: RecyclerView
    private lateinit var mealsRecyclerView: RecyclerView
    private lateinit var dateAdapter: DateAdapter
    private lateinit var mealHistoryAdapter: MealHistoryAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var searchBox: EditText
    private var selectedDate: String = ""
    private var allMeals: List<Meal> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_history)

        sessionManager = SessionManager(this)

        datesRecyclerView = findViewById(R.id.datesRecyclerView)
        mealsRecyclerView = findViewById(R.id.mealsRecyclerView)
        searchBox = findViewById(R.id.searchBox)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish()
        }

        setupSearchBox()
        setupDatesRecyclerView()
        setupMealsRecyclerView()
    }

    private fun setupSearchBox() {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMeals(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterMeals(query: String) {
        if (::mealHistoryAdapter.isInitialized) {
            val filteredList = if (query.isEmpty()) {
                allMeals
            } else {
                allMeals.filter { meal ->
                    meal.name.contains(query, ignoreCase = true)
                }
            }
            mealHistoryAdapter = MealHistoryAdapter(filteredList)
            mealsRecyclerView.adapter = mealHistoryAdapter
        }
    }

    private fun setupDatesRecyclerView() {
        val dates = generateLast7Days()

        dateAdapter = DateAdapter(dates) { position ->
            // Handle date selection and fetch meals for selected date
            selectedDate = dates[position].fullDate
            fetchMealsByDate(selectedDate)
        }

        datesRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@MealHistory, LinearLayoutManager.HORIZONTAL, false)
            adapter = dateAdapter
        }

        // Load today's meals by default
        if (dates.isNotEmpty()) {
            selectedDate = dates[0].fullDate
            fetchMealsByDate(selectedDate)
        }
    }

    private fun generateLast7Days(): List<DateItem> {
        val dates = mutableListOf<DateItem>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
        val yearFormat = SimpleDateFormat("yyyy", Locale.getDefault())

        for (i in 0 until 7) {
            val fullDate = dateFormat.format(calendar.time)
            val month = monthFormat.format(calendar.time)
            val day = dayFormat.format(calendar.time)
            val year = yearFormat.format(calendar.time)

            dates.add(DateItem(
                month = month,
                day = day,
                year = year,
                fullDate = fullDate,
                isSelected = i == 0
            ))

            calendar.add(Calendar.DAY_OF_MONTH, -1)
        }

        return dates
    }

    private fun setupMealsRecyclerView() {
        mealsRecyclerView.layoutManager = LinearLayoutManager(this)
        // Adapter will be set after fetching data
    }

    private fun fetchMealsByDate(date: String) {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_MEALS_BY_DATE_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    android.util.Log.d("MealHistory", "Server response: $response")

                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val mealsArray = json.getJSONArray("meals")
                        val meals = mutableListOf<Meal>()

                        for (i in 0 until mealsArray.length()) {
                            val mealObj = mealsArray.getJSONObject(i)
                            meals.add(
                                Meal(
                                    id = mealObj.getInt("id"),
                                    name = mealObj.getString("name"),
                                    calories = mealObj.getInt("calories"),
                                    carbs = mealObj.optString("carbs", "0"),
                                    protein = mealObj.optString("protein", "0"),
                                    imageUrl = if (mealObj.isNull("image_url")) null else mealObj.getString("image_url")
                                )
                            )
                        }

                        if (meals.isEmpty()) {
                            Toast.makeText(this, "No meals logged for this date", Toast.LENGTH_SHORT).show()
                        }

                        allMeals = meals
                        filterMeals(searchBox.text.toString())

                        mealHistoryAdapter = MealHistoryAdapter(meals)
                        mealsRecyclerView.adapter = mealHistoryAdapter
                    } else {
                        Toast.makeText(this, json.optString("message", "Failed to load meals"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("MealHistory", "Parse error: ${e.message}")
                    Toast.makeText(this, "Error loading meals", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                android.util.Log.e("MealHistory", "Network error: ${error.message}")
                Toast.makeText(this, "Failed to fetch meals: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["date"] = date
                android.util.Log.d("MealHistory", "Fetching meals for date: $date, user: $userId")
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<RelativeLayout>(R.id.rootLayout)
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
