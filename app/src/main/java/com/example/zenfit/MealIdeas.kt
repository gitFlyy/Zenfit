package com.example.zenfit

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.Volley

class MealIdeas : AppCompatActivity() {

    private lateinit var mealIdeasRecyclerView: RecyclerView
    private lateinit var mealIdeasAdapter: MealIdeasAdapter
    private lateinit var progressBar: ProgressBar
    private lateinit var searchBox: EditText
    private var allMealIdeas: List<Meal> = listOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_ideas)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish()
        }

        progressBar = ProgressBar(this)
        mealIdeasRecyclerView = findViewById(R.id.mealIdeasRecyclerView)
        mealIdeasRecyclerView.layoutManager = LinearLayoutManager(this)
        searchBox = findViewById(R.id.searchBox)

        setupSearchBox()
        loadMealIdeas()
    }

    private fun setupSearchBox() {
        searchBox.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                filterMealIdeas(s.toString())
            }

            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun filterMealIdeas(query: String) {
        if (allMealIdeas.isNotEmpty()) {
            val filteredList = if (query.isEmpty()) {
                allMealIdeas
            } else {
                allMealIdeas.filter { meal ->
                    meal.name.contains(query, ignoreCase = true)
                }
            }
            mealIdeasAdapter = MealIdeasAdapter(filteredList)
            mealIdeasRecyclerView.adapter = mealIdeasAdapter
        }
    }

    private fun loadMealIdeas() {
        val queue = Volley.newRequestQueue(this)
        val url = ApiConfig.GET_MEAL_IDEAS_URL

        val request = JsonArrayRequest(Request.Method.GET, url, null,
            { response ->
                try {
                    val meals = mutableListOf<Meal>()
                    for (i in 0 until response.length()) {
                        val obj = response.getJSONObject(i)
                        val id = obj.optInt("id", 0)
                        val name = obj.optString("name", "")
                        val calories = obj.optInt("calories", 0)
                        val carbs = obj.optString("carbs", "0")
                        val protein = obj.optString("protein", "0")
                        val image = obj.optString("image", "")
                        meals.add(Meal(id = id, name = name, calories = calories, carbs = carbs, protein = protein, imageUrl = if (image.isEmpty()) null else image))
                    }

                    allMealIdeas = meals
                    filterMealIdeas(searchBox.text.toString())
                } catch (e: Exception) {
                    Log.e("MealIdeas", "JSON parse error", e)
                    Toast.makeText(this, "Failed to parse server response", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("MealIdeas", "Volley error", error)
                Toast.makeText(this, "Failed to load meal ideas", Toast.LENGTH_LONG).show()
            })

        queue.add(request)
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", MODE_PRIVATE)
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
