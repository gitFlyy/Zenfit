package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import kotlin.compareTo
import kotlin.dec
import kotlin.div
import kotlin.printStackTrace
import kotlin.text.clear
import kotlin.text.compareTo
import kotlin.text.get
import kotlin.text.set
import kotlin.text.toInt
import kotlin.times
import kotlin.toString

class WorkoutLogging : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var exercises: MutableList<Exercise> = mutableListOf()
    private var currentExerciseIndex = 0
    private var countDownTimer: CountDownTimer? = null
    private var currentTime = 0L
    private var isTimerRunning = false
    private lateinit var workoutsRecyclerView: RecyclerView
    private val workoutsList = mutableListOf<Workout>()
    // UI Components
    private lateinit var exerciseName: TextView
    private lateinit var repsDisplay: TextView
    private lateinit var completedSetsValue: TextView
    private lateinit var weightLabel: TextView
    private lateinit var restTime: TextView
    private lateinit var repsValue: TextView
    private lateinit var setsValue: TextView
    private lateinit var currentTimeText: TextView
    private lateinit var timerSeekBar: SeekBar
    private lateinit var btnFavorite: ImageButton
    private lateinit var btnPlayPause: ImageButton
    private lateinit var workoutAdapter: WorkoutVerticalAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_logging)

        sessionManager = SessionManager(this)
        initializeViews()
        setupListeners()
        if (intent.hasExtra("workout_name")) {
            loadWorkoutFromIntent()
        } else {
            fetchExercises()
        }
    }
    private fun loadWorkoutFromIntent() {
        val workoutName = intent.getStringExtra("workout_name") ?: "Workout"
        val duration = intent.getIntExtra("workout_duration", 1800)
        val reps = intent.getIntExtra("workout_reps", 10)
        val sets = intent.getIntExtra("workout_sets", 5)
        val weight = intent.getIntExtra("workout_weight", 150)
        val restTime = intent.getIntExtra("workout_rest_time", 60)

        exercises.add(
            Exercise(
                id = 0,
                name = workoutName,
                reps = reps,
                sets = sets,
                weight = weight,
                restTime = restTime,
                completedSets = 0,
                isFavorite = false,
                duration = duration
            )
        )

        updateUI()
    }



    private fun initializeViews() {
        exerciseName = findViewById(R.id.exerciseName)
        repsDisplay = findViewById(R.id.repsDisplay)
        completedSetsValue = findViewById(R.id.completedSetsValue)
        weightLabel = findViewById(R.id.weightLabel)
        restTime = findViewById(R.id.restTime)
        repsValue = findViewById(R.id.repsValue)
        setsValue = findViewById(R.id.setsValue)
        currentTimeText = findViewById(R.id.currentTime)
        timerSeekBar = findViewById(R.id.timerSeekBar)
        btnFavorite = findViewById(R.id.btnFavorite)
        btnPlayPause = findViewById(R.id.btnPlayPause)

        workoutsRecyclerView = findViewById(R.id.workoutsRecyclerView)
        setupWorkoutsRecyclerView()
    }
    private fun setupWorkoutsRecyclerView() {
        workoutAdapter = WorkoutVerticalAdapter(workoutsList) { workout ->
            loadWorkoutData(workout)
        }
        workoutsRecyclerView.adapter = workoutAdapter
        workoutsRecyclerView.layoutManager = LinearLayoutManager(this)
        fetchWorkoutsForBottomList()
    }

    private fun fetchWorkoutsForBottomList() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_WORKOUTS_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val workoutsArray = json.getJSONArray("workouts")
                        workoutsList.clear()

                        for (i in 0 until workoutsArray.length()) {
                            val obj = workoutsArray.getJSONObject(i)
                            workoutsList.add(
                                Workout(
                                    name = obj.getString("name"),
                                    duration = obj.getInt("duration"),
                                    reps = obj.getInt("reps"),
                                    sets = obj.getInt("sets"),
                                    weight = obj.optInt("weight", 150),
                                    restTime = obj.optInt("rest_time", 60),
                                    exerciseCount = 1
                                )
                            )
                        }

                        workoutAdapter.updateWorkouts(workoutsList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading workouts: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Failed to fetch workouts: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams() = hashMapOf("user_id" to userId)
        }

        Volley.newRequestQueue(this).add(request)
    }


    private fun loadWorkoutData(workout: Workout) {
        if (exercises.isEmpty()) {
            exercises.add(
                Exercise(
                    id = 0,
                    name = workout.name,
                    reps = workout.reps,
                    sets = workout.sets,
                    weight = workout.weight,
                    restTime = workout.restTime,
                    completedSets = 0,
                    isFavorite = false,
                    duration = workout.duration
                )
            )
        } else {
            exercises[currentExerciseIndex] = Exercise(
                id = exercises[currentExerciseIndex].id,
                name = workout.name,
                reps = workout.reps,
                sets = workout.sets,
                weight = workout.weight,
                restTime = workout.restTime,
                completedSets = 0,
                isFavorite = exercises[currentExerciseIndex].isFavorite,
                duration = workout.duration
            )
        }

        resetTimer()
        updateUI()
        Toast.makeText(this, "Loaded ${workout.name}", Toast.LENGTH_SHORT).show()
    }
    private fun setupListeners() {
        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<ImageButton>(R.id.navHome).setOnClickListener {
            startActivity(Intent(this, Home::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.navWorkout).setOnClickListener {
            startActivity(Intent(this, WorkoutLibrary::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.navAdd).setOnClickListener {
            startActivity(Intent(this, UploadPost::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.navCalendar).setOnClickListener {
            startActivity(Intent(this, CalendarActivity::class.java))
            finish()
        }

        findViewById<ImageButton>(R.id.navProfile).setOnClickListener {
            startActivity(Intent(this, Profile::class.java))
            finish()
        }

        // Set navigation buttons
        findViewById<ImageButton>(R.id.btnNext).setOnClickListener {
            if (exercises.isEmpty()) return@setOnClickListener

            val current = exercises[currentExerciseIndex]

            // Check if there are sets remaining
            if (current.sets <= 0) {
                Toast.makeText(this, "Already at the last set", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Move to next set - decrease total sets
            exercises[currentExerciseIndex] = current.copy(
                completedSets = current.completedSets + 1,
                sets = current.sets - 1
            )
            completedSetsValue.text = "${exercises[currentExerciseIndex].completedSets}"
            setsValue.text = exercises[currentExerciseIndex].sets.toString()

            // Reset reps for the next set
            currentReps = current.reps
            repsValue.text = currentReps.toString()

            resetTimer()
            Toast.makeText(this, "Moved to next set", Toast.LENGTH_SHORT).show()
        }


        findViewById<ImageButton>(R.id.btnPrevious).setOnClickListener {
            if (exercises.isEmpty()) return@setOnClickListener

            val current = exercises[currentExerciseIndex]

            if (current.completedSets > 0) {
                // Increase total sets and decrease completed sets
                exercises[currentExerciseIndex] = current.copy(
                    completedSets = current.completedSets - 1,
                    sets = current.sets + 1
                )
                completedSetsValue.text = "${exercises[currentExerciseIndex].completedSets}"
                setsValue.text = exercises[currentExerciseIndex].sets.toString()

                currentReps = current.reps
                repsValue.text = currentReps.toString()

                resetTimer()
                Toast.makeText(this, "Moved to previous set", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Already at first set", Toast.LENGTH_SHORT).show()
            }
        }


        // Reps and Sets controls
        findViewById<ImageButton>(R.id.btnIncreaseReps).setOnClickListener {
            if (exercises.isNotEmpty()) {
                val current = exercises[currentExerciseIndex]
                exercises[currentExerciseIndex] = current.copy(reps = current.reps + 1)
                updateUI()
            }
        }

        findViewById<ImageButton>(R.id.btnDecreaseReps).setOnClickListener {
            if (exercises.isNotEmpty()) {
                val current = exercises[currentExerciseIndex]
                if (current.reps > 0) {
                    exercises[currentExerciseIndex] = current.copy(reps = current.reps - 1)
                    updateUI()
                }
            }
        }

        findViewById<ImageButton>(R.id.btnIncreaseSets).setOnClickListener {
            if (exercises.isNotEmpty()) {
                val current = exercises[currentExerciseIndex]
                exercises[currentExerciseIndex] = current.copy(sets = current.sets + 1)
                updateUI()
            }
        }

        findViewById<ImageButton>(R.id.btnDecreaseSets).setOnClickListener {
            if (exercises.isNotEmpty()) {
                val current = exercises[currentExerciseIndex]
                if (current.sets > 0) {
                    exercises[currentExerciseIndex] = current.copy(sets = current.sets - 1)
                    updateUI()
                }
            }
        }

        btnFavorite.setOnClickListener {
            if (exercises.isNotEmpty()) {
                val current = exercises[currentExerciseIndex]
                exercises[currentExerciseIndex] = current.copy(isFavorite = !current.isFavorite)
                updateUI()
            }
        }

        btnPlayPause.setOnClickListener { toggleTimer() }

        // REMOVE THE DUPLICATE LISTENERS THAT WERE HERE
    }

    private fun completeSet() {
        if (exercises.isEmpty()) return

        val current = exercises[currentExerciseIndex]
        if (current.completedSets < current.sets) {
            exercises[currentExerciseIndex] = current.copy(completedSets = current.completedSets + 1)
            updateUI()

            // Start rest timer only if there are more sets to complete
            if (exercises[currentExerciseIndex].completedSets < current.sets) {
                resetTimer()
                startTimer()
                Toast.makeText(this, "Set completed! Rest time started", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "All sets completed!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchExercises() {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.GET_EXERCISES_URL

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        val exercisesArray = json.getJSONArray("exercises")
                        exercises.clear()

                        for (i in 0 until exercisesArray.length()) {
                            val exerciseObj = exercisesArray.getJSONObject(i)
                            exercises.add(
                                Exercise(
                                    id = exerciseObj.getInt("id"),
                                    name = exerciseObj.getString("name"),
                                    reps = exerciseObj.getInt("reps"),
                                    sets = exerciseObj.getInt("sets"),
                                    weight = exerciseObj.getInt("weight"),
                                    restTime = exerciseObj.getInt("rest_time"),
                                    completedSets = exerciseObj.getInt("completed_sets"),
                                    isFavorite = exerciseObj.getBoolean("is_favorite"),
                                    duration = exerciseObj.optInt("duration", 30)
                                )
                            )
                        }

                        if (exercises.isEmpty()) {
                            Toast.makeText(this, "No exercises logged yet", Toast.LENGTH_SHORT).show()
                        } else {
                            updateUI()
                        }
                    } else {
                        Toast.makeText(this, json.optString("message", "Failed to load exercises"), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                Toast.makeText(this, "Failed to fetch exercises: ${error.message}", Toast.LENGTH_SHORT).show()
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

    private fun updateUI() {
        if (exercises.isEmpty()) return

        val exercise = exercises[currentExerciseIndex]
        exerciseName.text = exercise.name
        repsDisplay.text = "Reps: ${exercise.reps}"
        completedSetsValue.text = "${exercise.completedSets}"
        weightLabel.text = "Weight: ${exercise.weight}lbs"
        restTime.text = "Rest: ${exercise.restTime} seconds"
        repsValue.text = exercise.reps.toString()
        setsValue.text = exercise.sets.toString()

        findViewById<TextView>(R.id.totalTime).text = formatDuration(exercise.duration)
        timerSeekBar.max = exercise.restTime

        btnFavorite.setImageResource(
            if (exercise.isFavorite) R.drawable.baseline_star_24
            else R.drawable.baseline_star_24
        )

    }


    private fun formatDuration(totalSeconds: Int): String {
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format("%d:%02d", minutes, seconds)
    }
    private var isWorkoutRunning = false // Track if doing workout or resting
    private var currentReps = 0 // Track current reps remaining

    private fun toggleTimer() {
        if (isTimerRunning) {
            pauseTimer()
        } else {
            startWorkoutTimer()
        }
    }

    private fun startWorkoutTimer() {
        if (exercises.isEmpty()) return

        val exercise = exercises[currentExerciseIndex]

        // Initialize reps if starting fresh
        if (currentReps == 0) {
            currentReps = exercise.reps
        }

        if (currentReps <= 0) {
            Toast.makeText(this, "Complete the rest period first", Toast.LENGTH_SHORT).show()
            return
        }

        isWorkoutRunning = true
        val totalTime = exercise.duration * 1000L

        // Set SeekBar max to duration
        timerSeekBar.max = exercise.duration

        countDownTimer = object : CountDownTimer(totalTime - currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentTime = totalTime - millisUntilFinished
                val seconds = (currentTime / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                currentTimeText.text = String.format("%d:%02d", minutes, secs)
                // Progress from 0 to duration
                timerSeekBar.progress = seconds
            }

            override fun onFinish() {
                // One rep completed
                currentReps--
                repsValue.text = currentReps.toString()
                currentTime = 0L
                timerSeekBar.progress = 0

                if (currentReps > 0) {
                    // More reps to do - auto start next rep
                    Toast.makeText(this@WorkoutLogging, "Rep completed! Next rep starting...", Toast.LENGTH_SHORT).show()
                    startWorkoutTimer()
                } else {
                    // All reps done - complete set and start rest
                    completeSetAndRest()
                }
            }
        }.start()

        isTimerRunning = true
        btnPlayPause.setImageResource(R.drawable.btnplaypause_img)
    }

    private fun completeSetAndRest() {
        if (exercises.isEmpty()) return

        val current = exercises[currentExerciseIndex]

        // Check if there are more sets BEFORE modifying
        val hasMoreSets = current.sets > 1

        // Increase completed sets and decrease total sets
        exercises[currentExerciseIndex] = current.copy(
            completedSets = current.completedSets + 1,
            sets = current.sets - 1
        )

        // Update both displays
        completedSetsValue.text = "${exercises[currentExerciseIndex].completedSets}"
        setsValue.text = exercises[currentExerciseIndex].sets.toString()

        if (hasMoreSets) {
            // More sets remaining - start rest timer
            Toast.makeText(this, "Set completed! Rest time started", Toast.LENGTH_SHORT).show()
            startRestTimer()
        } else {
            // All sets completed
            saveToWorkoutHistory(current)
            isTimerRunning = false
            isWorkoutRunning = false
            btnPlayPause.setImageResource(R.drawable.play)
            Toast.makeText(this, "All sets completed! Great job!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun saveToWorkoutHistory(exercise: Exercise) {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.SAVE_WORKOUT_HISTORY_URL

        // Generate random calories between 0-500
        val caloriesBurned = (0..500).random()

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, "Exercise completed! Burned $caloriesBurned calories", Toast.LENGTH_SHORT).show()

                        exercises.removeAt(currentExerciseIndex)

                        if (exercises.isEmpty()) {
                            Toast.makeText(this, "All exercises completed!", Toast.LENGTH_LONG).show()
                            finish()
                        } else {
                            if (currentExerciseIndex >= exercises.size) {
                                currentExerciseIndex = exercises.size - 1
                            }
                            updateUI()
                            resetTimer()
                        }
                    } else {
                        Toast.makeText(this, "Failed to save to history", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                return hashMapOf(
                    "user_id" to userId,
                    "exercise_name" to exercise.name,
                    "reps" to exercise.reps.toString(),
                    "sets" to (exercise.completedSets + 1).toString(),
                    "weight" to exercise.weight.toString(),
                    "duration" to exercise.duration.toString(),
                    "rest_time" to exercise.restTime.toString(),
                    "calories_burned" to caloriesBurned.toString(),
                    "completed_date" to System.currentTimeMillis().toString()
                )
            }
        }

        Volley.newRequestQueue(this).add(request)
    }



    private fun startRestTimer() {
        if (exercises.isEmpty()) return

        val exercise = exercises[currentExerciseIndex]
        isWorkoutRunning = false
        currentTime = 0L
        val restMillis = exercise.restTime * 1000L

        countDownTimer = object : CountDownTimer(restMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                currentTimeText.text = String.format("%d:%02d", minutes, secs)
                timerSeekBar.progress = exercise.restTime - seconds
            }

            override fun onFinish() {
                // Reset for next set
                currentReps = exercise.reps
                repsValue.text = currentReps.toString()
                currentTime = 0L
                timerSeekBar.progress = 0
                isTimerRunning = false
                btnPlayPause.setImageResource(R.drawable.play)
                Toast.makeText(this@WorkoutLogging, "Rest completed! Ready for next set", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
        btnPlayPause.setImageResource(R.drawable.btnplaypause_img)
    }

    private fun pauseTimer() {
        countDownTimer?.cancel()
        isTimerRunning = false
        btnPlayPause.setImageResource(R.drawable.play)
    }

    private fun resetTimer() {
        countDownTimer?.cancel()
        currentTime = 0L
        isTimerRunning = false
        isWorkoutRunning = false

        if (exercises.isNotEmpty()) {
            currentReps = exercises[currentExerciseIndex].reps
            repsValue.text = currentReps.toString()
        }

        currentTimeText.text = "0:00"
        timerSeekBar.progress = 0
        btnPlayPause.setImageResource(R.drawable.play)
    }

    private fun startTimer() {
        if (exercises.isEmpty()) return

        val exercise = exercises[currentExerciseIndex]
        val totalTime = exercise.restTime * 1000L

        countDownTimer = object : CountDownTimer(totalTime - currentTime, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                currentTime = totalTime - millisUntilFinished
                val seconds = (currentTime / 1000).toInt()
                val minutes = seconds / 60
                val secs = seconds % 60
                currentTimeText.text = String.format("%d:%02d", minutes, secs)
                timerSeekBar.progress = seconds
            }

            override fun onFinish() {
                isTimerRunning = false
                btnPlayPause.setImageResource(R.drawable.play)
                Toast.makeText(this@WorkoutLogging, "Rest time completed!", Toast.LENGTH_SHORT).show()
            }
        }.start()

        isTimerRunning = true
        btnPlayPause.setImageResource(R.drawable.btnplaypause_img)
    }



    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        findViewById<RelativeLayout>(R.id.main).setBackgroundResource(
            if (isDarkMode) R.drawable.zenfit_background else R.drawable.zenfit_background_light
        )
    }

    override fun onResume() {
        super.onResume()
        applyTheme()
    }

    override fun onDestroy() {
        super.onDestroy()
        countDownTimer?.cancel()
    }
}
