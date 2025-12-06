package com.example.zenfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutLibraryAdapter(
    private var workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit,
    private val onSelectionChanged: (List<Int>) -> Unit
) : RecyclerView.Adapter<WorkoutLibraryAdapter.ViewHolder>() {

    private var filteredWorkouts: List<Workout> = workouts
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

        holder.workoutName.text = workout.name
        holder.startNow.text = "Start now"
        holder.durationText.text = "Duration:\n${formatDuration(workout.duration)}"
        holder.repsText.text = "Reps:\n${workout.reps}"
        holder.setsText.text = "Sets:\n${workout.sets}"

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

        // Handle item click
        holder.itemView.setOnClickListener {
            if (isSelectionMode) {
                holder.checkboxSelect.isChecked = !holder.checkboxSelect.isChecked
            } else {
                onWorkoutClick(workout)
            }
        }
    }

    override fun getItemCount() = filteredWorkouts.size

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        filteredWorkouts = newWorkouts
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        filteredWorkouts = if (query.isEmpty()) {
            workouts
        } else {
            workouts.filter {
                it.name.contains(query, ignoreCase = true)
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

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            minutes == 0 -> "${seconds}s"
            seconds == 0 -> "${minutes}min"
            else -> "${minutes}min ${seconds}s"
        }
    }
}
