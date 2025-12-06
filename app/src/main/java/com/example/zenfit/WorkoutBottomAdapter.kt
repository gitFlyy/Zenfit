package com.example.zenfit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WorkoutBottomAdapter(
    private var workouts: List<Workout>,
    private val onWorkoutClick: (Workout) -> Unit
) : RecyclerView.Adapter<WorkoutBottomAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.workoutNameBottom)
        val duration: TextView = view.findViewById(R.id.workoutDurationBottom)
        val reps: TextView = view.findViewById(R.id.workoutRepsBottom)
        val sets: TextView = view.findViewById(R.id.workoutSetsBottom)
        val weight: TextView = view.findViewById(R.id.workoutWeightBottom)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_bottom, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val workout = workouts[position]
        holder.name.text = workout.name
        holder.duration.text = formatDuration(workout.duration)
        holder.reps.text = "${workout.reps} reps"
        holder.sets.text = "${workout.sets} sets"
        holder.weight.text = "${workout.weight} lbs"

        holder.itemView.setOnClickListener { onWorkoutClick(workout) }
    }

    override fun getItemCount() = workouts.size

    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        return when {
            minutes == 0 -> "${totalSeconds}s"
            totalSeconds % 60 == 0 -> "${minutes}min"
            else -> "${minutes}min ${totalSeconds % 60}s"
        }
    }

    fun updateWorkouts(newWorkouts: List<Workout>) {
        workouts = newWorkouts
        notifyDataSetChanged()
    }
}
