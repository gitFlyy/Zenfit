package com.example.zenfit

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONArray
import org.json.JSONObject

class Profile : AppCompatActivity() {
    
    private lateinit var sessionManager: SessionManager
    private lateinit var postsRecyclerView: RecyclerView
    private lateinit var postsProgressBar: ProgressBar
    private lateinit var emptyStateText: TextView
    private lateinit var userName: TextView
    private lateinit var profileImage: CircleImageView
    private lateinit var postAdapter: PostAdapter
    private val postsList = mutableListOf<Post>()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // Initialize SessionManager
        sessionManager = SessionManager(this)
        
        // Initialize views
        userName = findViewById(R.id.userName)
        profileImage = findViewById(R.id.profileImage)
        postsRecyclerView = findViewById(R.id.postsRecyclerView)
        postsProgressBar = findViewById(R.id.postsProgressBar)
        emptyStateText = findViewById(R.id.emptyStateText)
        
        // Setup RecyclerView
        postAdapter = PostAdapter(postsList)
        postsRecyclerView.layoutManager = GridLayoutManager(this, 2)
        postsRecyclerView.adapter = postAdapter
        
        // Settings button
        val settingsBtn = findViewById<ImageView>(R.id.settingsBtn)
        settingsBtn.setOnClickListener {
            val intent = Intent(this, Settings::class.java)
            startActivity(intent)
        }

        val editprofile=findViewById<TextView>(R.id.editProfileBtn)
        editprofile.setOnClickListener {
            val intent = Intent(this, Account::class.java)
            startActivity(intent)
        }


        // Bottom navigation
        val navHome = findViewById<ImageView>(R.id.navHome)
        val navWorkout = findViewById<ImageView>(R.id.navWorkout)
        val navAdd = findViewById<ImageView>(R.id.navAdd)
        val navCalendar = findViewById<ImageView>(R.id.navCalendar)
        navHome.setOnClickListener {
            val intent = Intent(this, Home::class.java)
            startActivity(intent)
            finish()
        }

        navWorkout.setOnClickListener {
            val intent = Intent(this, WorkoutLogging::class.java)
            startActivity(intent)
        }

        navAdd.setOnClickListener {
            val intent = Intent(this, CreateWorkout::class.java)
            startActivity(intent)
        }
        navCalendar.setOnClickListener {
            val intent = Intent(this, CalendarActivity::class.java)
            startActivity(intent)
        }

        fetchProfileData()
        fetchUserPosts()
    }
    
    private fun fetchProfileData() {
        val userId = sessionManager.getUserId()
        
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.GET_PROFILE_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val statusCode = jsonResponse.getInt("statuscode")
                    
                    if (statusCode == 200) {
                        val data = jsonResponse.getJSONObject("data")
                        val username = data.getString("username")
                        val profileImageBase64 = data.optString("profile_image", "")

                        // Update UI with profile data
                        userName.text = username
                        
                        // Decode and display profile image if available
                        if (profileImageBase64.isNotEmpty() && profileImageBase64 != "null") {
                            try {
                                val decodedBytes = Base64.decode(profileImageBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                profileImage.setImageBitmap(bitmap)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                // Keep default profile image on error
                            }
                        }

                    } else {
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error parsing profile data", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
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
    
    private fun fetchUserPosts() {
        val userId = sessionManager.getUserId()
        
        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }
        
        // Show loading
        postsProgressBar.visibility = View.VISIBLE
        postsRecyclerView.visibility = View.GONE
        emptyStateText.visibility = View.GONE
        
        val request = object : StringRequest(
            Request.Method.POST,
            ApiConfig.GET_USER_POSTS_URL,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val statusCode = jsonResponse.getInt("statuscode")
                    
                    if (statusCode == 200) {
                        val dataArray = jsonResponse.getJSONArray("data")
                        postsList.clear()
                        
                        for (i in 0 until dataArray.length()) {
                            val postObj = dataArray.getJSONObject(i)
                            val post = Post(
                                id = postObj.getInt("id"),
                                userId = postObj.getString("user_id"),
                                imageData = postObj.getString("image_data"),
                                createdAt = postObj.getString("created_at")
                            )
                            postsList.add(post)
                        }
                        
                        // Update UI
                        postsProgressBar.visibility = View.GONE
                        
                        if (postsList.isEmpty()) {
                            emptyStateText.visibility = View.VISIBLE
                            postsRecyclerView.visibility = View.GONE
                        } else {
                            emptyStateText.visibility = View.GONE
                            postsRecyclerView.visibility = View.VISIBLE
                            postAdapter.updatePosts(postsList)
                        }
                        
                    } else {
                        postsProgressBar.visibility = View.GONE
                        emptyStateText.visibility = View.VISIBLE
                        val message = jsonResponse.getString("message")
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    postsProgressBar.visibility = View.GONE
                    emptyStateText.visibility = View.VISIBLE
                    Toast.makeText(this, "Error loading posts", Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                error.printStackTrace()
                postsProgressBar.visibility = View.GONE
                emptyStateText.visibility = View.VISIBLE
                Toast.makeText(this, "Network error: ${error.message}", Toast.LENGTH_SHORT).show()
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

        val rootLayout = findViewById<LinearLayout>(R.id.main)
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