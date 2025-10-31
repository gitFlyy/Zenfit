package com.example.zenfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealIdeasAdapter(private val meals: List<Meal>) :
    RecyclerView.Adapter<MealIdeasAdapter.MealIdeaViewHolder>() {

    class MealIdeaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val caloriesValue: TextView = itemView.findViewById(R.id.caloriesValue)
        val carbsValue: TextView = itemView.findViewById(R.id.carbsValue)
        val proteinValue: TextView = itemView.findViewById(R.id.proteinValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealIdeaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_idea, parent, false)
        return MealIdeaViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealIdeaViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.name
        holder.caloriesValue.text = "${meal.calories} kcal"
        holder.carbsValue.text = "${meal.carbs}g"
        holder.proteinValue.text = "${meal.protein}g"
        holder.mealImage.setImageResource(meal.imageResId)
    }

    override fun getItemCount() = meals.size
}
