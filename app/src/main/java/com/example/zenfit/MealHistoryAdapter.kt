package com.example.zenfit

import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MealHistoryAdapter(
    private val meals: List<Meal>
) : RecyclerView.Adapter<MealHistoryAdapter.MealViewHolder>() {

    inner class MealViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mealImage: ImageView = itemView.findViewById(R.id.mealImage)
        val mealName: TextView = itemView.findViewById(R.id.mealName)
        val caloriesValue: TextView = itemView.findViewById(R.id.caloriesValue)
        val carbsValue: TextView = itemView.findViewById(R.id.carbsValue)
        val proteinValue: TextView = itemView.findViewById(R.id.proteinValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MealViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_meal_history, parent, false)
        return MealViewHolder(view)
    }

    override fun onBindViewHolder(holder: MealViewHolder, position: Int) {
        val meal = meals[position]
        holder.mealName.text = meal.name
        holder.caloriesValue.text = "${meal.calories} kcal"
        holder.carbsValue.text = meal.carbs
        holder.proteinValue.text = meal.protein

        // Decode base64 image or use placeholder
        if (!meal.imageUrl.isNullOrEmpty()) {
            try {
                val imageBytes = Base64.decode(meal.imageUrl, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                holder.mealImage.setImageBitmap(bitmap)
                holder.mealImage.scaleType = ImageView.ScaleType.CENTER_CROP
            } catch (e: Exception) {
                e.printStackTrace()
                holder.mealImage.setImageResource(R.drawable.meal_placeholder)
            }
        } else {
            holder.mealImage.setImageResource(R.drawable.meal_placeholder)
        }
    }

    override fun getItemCount() = meals.size
}
