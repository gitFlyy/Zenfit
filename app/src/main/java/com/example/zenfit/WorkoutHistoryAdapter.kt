package com.example.zenfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class WorkoutHistoryAdapter(
    private var workouts: List<WorkoutHistoryItem>,
    private val onSelectionChanged: (List<Int>) -> Unit
) : RecyclerView.Adapter<WorkoutHistoryAdapter.ViewHolder>() {

    private var filteredWorkouts: List<WorkoutHistoryItem> = workouts
    private val selectedItems = mutableSetOf<Int>()
    var isSelectionMode = false
        set(value) {
            field = value
            if (!value) {
                selectedItems.clear()
            }
            notifyDataSetChanged()
        }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val workoutName: TextView = view.findViewById(R.id.workoutName)
        val startNow: TextView = view.findViewById(R.id.startNow)
        val durationText: TextView = view.findViewById(R.id.durationText)
        val repsText: TextView = view.findViewById(R.id.repsText)
        val setsText: TextView = view.findViewById(R.id.setsText)
        val checkboxSelect: CheckBox = view.findViewById(R.id.checkboxSelect)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = filteredWorkouts[position]

        holder.workoutName.text = workout.exerciseName
        holder.startNow.text = formatDate(workout.completedDate)
        holder.durationText.text = "${workout.duration / 60} min"
        holder.repsText.text = "${workout.reps} reps"
        holder.setsText.text = "${workout.sets} sets"

        // Show/hide checkbox based on selection mode
        holder.checkboxSelect.visibility = if (isSelectionMode) View.VISIBLE else View.GONE
        holder.checkboxSelect.isChecked = selectedItems.contains(workout.id)

        // Handle checkbox clicks
        holder.checkboxSelect.setOnCheckedChangeListener(null)
        holder.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(workout.id)
            } else {
                selectedItems.remove(workout.id)
            }
            onSelectionChanged(selectedItems.toList())
        }

        // Handle item long press to enable selection mode
        holder.itemView.setOnLongClickListener {
            if (!isSelectionMode) {
                isSelectionMode = true
                selectedItems.add(workout.id)
                onSelectionChanged(selectedItems.toList())
            }
            true
        }

        // Handle item click in selection mode
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                holder.checkboxSelect.isChecked = !holder.checkboxSelect.isChecked
            }
        }
    }

    override fun getItemCount() = filteredWorkouts.size

    fun updateWorkouts(newWorkouts: List<WorkoutHistoryItem>) {
        workouts = newWorkouts
        filteredWorkouts = newWorkouts
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredWorkouts = if (query.isEmpty()) {
            workouts
        } else {
            workouts.filter {
                it.exerciseName.contains(query, ignoreCase = true)
            }
        }
        notifyDataSetChanged()
    }

    fun getSelectedIds() = selectedItems.toList()

    fun clearSelection() {
        selectedItems.clear()
        isSelectionMode = false
        notifyDataSetChanged()
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }
}
