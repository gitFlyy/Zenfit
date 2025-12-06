package com.example.zenfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutVerticalAdapter(
    private var workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutVerticalAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.workoutName)
        val duration: TextView = view.findViewById(R.id.durationText)
        val reps: TextView = view.findViewById(R.id.repsText)
        val sets: TextView = view.findViewById(R.id.setsText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_card, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.name.text = workout.name
        holder.duration.text = "Duration:\n${formatDuration(workout.duration)}"
        holder.reps.text = "Reps:\n${workout.reps}"
        holder.sets.text = "Sets:\n${workout.sets}"

        holder.itemView.setOnClickListener { onWorkoutClick(workout) }
    }

    override fun getItemCount() = workouts.size

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return when {
            minutes == 0 -> "${seconds}s"
            seconds == 0 -> "${minutes}min"
            else -> "${minutes}min ${seconds}s"
        }
    }

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}
