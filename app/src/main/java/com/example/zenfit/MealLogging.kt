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

class MealLogging : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var mealLogAdapter: MealLogAdapter
    private lateinit var sessionManager: SessionManager
    private lateinit var searchBox: EditText
    private var allMeals: List<Meal> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_logging)

        sessionManager = SessionManager(this)
        searchBox = findViewById(R.id.searchBox)

        setupBackButton()
        setupSearchBox()
        setupRecyclerView()
        fetchMeals()
    }

    private fun setupBackButton() {
        findViewById<ImageView>(R.id.backArrow).setOnClickListener {
            finish()
        }
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
        if (::mealLogAdapter.isInitialized) {
            val filteredList = if (query.isEmpty()) {
                allMeals
            } else {
                allMeals.filter { meal ->
                    meal.name.contains(query, ignoreCase = true)
                }
            }
            mealLogAdapter = MealLogAdapter(filteredList)
            recyclerView.adapter = mealLogAdapter
        }
    }

    private fun setupRecyclerView() {
        recyclerView = findViewById(R.id.mealsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun fetchMeals() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_MEALS_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
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
                            Toast.makeText(this, "No meals logged yet", Toast.LENGTH_SHORT).show()
                        }

                        allMeals = meals
                        filterMeals(searchBox.text.toString())

                        mealLogAdapter = MealLogAdapter(meals)
                        recyclerView.adapter = mealLogAdapter
                    } else {
                        Toast.makeText(this, json.optString("message", "Failed to load meals"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch meals: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
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
