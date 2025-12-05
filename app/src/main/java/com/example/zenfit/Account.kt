package com.example.zenfit

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class Account : AppCompatActivity() {
    private lateinit var usernameField: EditText
    private lateinit var firstNameField: EditText
    private lateinit var lastNameField: EditText
    private lateinit var emailField: EditText
    private lateinit var weightField: EditText
    private lateinit var heightField: EditText
    private lateinit var saveChangesBtn: Button
    private lateinit var deleteAccountBtn: TextView
    private lateinit var backBtn: ImageView
    private lateinit var profileImageView: CircleImageView
    private lateinit var cameraIcon: ImageView
    private lateinit var sessionManager: SessionManager
    private var profileImageBase64: String? = null
    private var hasImageChanged = false

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchCamera()
        } else {
            Toast.makeText(this, "Camera permission is required to take photos", Toast.LENGTH_SHORT).show()
        }
    }

    private val galleryLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                handleImageSelection(uri)
            }
        }
    }

    private val cameraLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val imageBitmap = result.data?.extras?.get("data") as? Bitmap
            imageBitmap?.let {
                profileImageView.setImageBitmap(it)
                profileImageBase64 = bitmapToBase64(it)
                hasImageChanged = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_account)

        sessionManager = SessionManager(this)

        usernameField = findViewById(R.id.usernameField)
        firstNameField = findViewById(R.id.firstNameField)
        lastNameField = findViewById(R.id.lastNameField)
        emailField = findViewById(R.id.emailField)
        weightField = findViewById(R.id.weightField)
        heightField = findViewById(R.id.heightField)
        saveChangesBtn = findViewById(R.id.saveChangesBtn)
        deleteAccountBtn = findViewById(R.id.deleteAccountBtn)
        backBtn = findViewById(R.id.backBtn)
        profileImageView = findViewById(R.id.profileImageView)
        cameraIcon = findViewById(R.id.cameraIcon)

        backBtn.setOnClickListener { finish() }

        // Handle profile picture clicks
        cameraIcon.setOnClickListener {
            showImageSourceDialog()
        }

        profileImageView.setOnClickListener {
            showImageSourceDialog()
        }

        saveChangesBtn.setOnClickListener {
            saveChanges()
        }

        deleteAccountBtn.setOnClickListener {
            showDeleteConfirmationDialog()
        }

        loadUserData()
    }

    private fun showImageSourceDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Update Profile Picture")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> openCamera()
                1 -> openGallery()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun openCamera() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                launchCamera()
            }
            else -> {
                requestCameraPermission.launch(Manifest.permission.CAMERA)
            }
        }
    }

    private fun launchCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraLauncher.launch(intent)
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryLauncher.launch(intent)
    }

    private fun handleImageSelection(uri: Uri) {
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            profileImageView.setImageBitmap(bitmap)
            profileImageBase64 = bitmapToBase64(bitmap)
            hasImageChanged = true
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun bitmapToBase64(bitmap: Bitmap): String {
        // Resize bitmap if it's too large (max 800x800 pixels)
        val maxDimension = 800
        val width = bitmap.width
        val height = bitmap.height

        val resizedBitmap = if (width > maxDimension || height > maxDimension) {
            val scale = Math.min(maxDimension.toFloat() / width, maxDimension.toFloat() / height)
            val newWidth = (width * scale).toInt()
            val newHeight = (height * scale).toInt()
            Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
        } else {
            bitmap
        }

        val outputStream = ByteArrayOutputStream()
        // Compress to JPEG with 60% quality (good balance between size and quality)
        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun loadUserData() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.GET_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        val userData = jsonResponse.getJSONObject("data")
                        usernameField.setText(userData.optString("username", ""))
                        firstNameField.setText(userData.optString("first_name", ""))
                        lastNameField.setText(userData.optString("last_name", ""))
                        emailField.setText(userData.optString("email", ""))
                        weightField.setText(userData.optString("weight", ""))
                        heightField.setText(userData.optString("height", ""))

                        // Load profile image if available
                        val profileImageBase64Str = userData.optString("profile_image", "")
                        if (profileImageBase64Str.isNotEmpty() && profileImageBase64Str != "null") {
                            try {
                                // Remove any whitespace or newlines that might be in the base64 string
                                val cleanBase64 = profileImageBase64Str.replace("\\s".toRegex(), "")
                                val decodedBytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                val bitmap = BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                if (bitmap != null) {
                                    profileImageView.setImageBitmap(bitmap)
                                }
                            } catch (e: Exception) {
                                e.printStackTrace()
                                Toast.makeText(this@Account, "Failed to load profile image", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(
                            this@Account,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error loading data: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun saveChanges() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val username = usernameField.text.toString().trim()
        val firstName = firstNameField.text.toString().trim()
        val lastName = lastNameField.text.toString().trim()
        val email = emailField.text.toString().trim()
        val weight = weightField.text.toString().trim()
        val height = heightField.text.toString().trim()

        if (username.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Username and email are required", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.UPDATE_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")

                // Build parameters
                val params = mutableMapOf<String, String>()
                params["user_id"] = userId
                params["username"] = username
                params["email"] = email
                params["first_name"] = firstName
                params["last_name"] = lastName
                params["weight"] = weight
                params["height"] = height

                // Add profile image if it was changed
                if (hasImageChanged && !profileImageBase64.isNullOrEmpty()) {
                    params["profile_image"] = profileImageBase64!!
                }

                // Convert params to URL encoded format
                val postData = params.map { "${it.key}=${java.net.URLEncoder.encode(it.value, "UTF-8")}" }
                    .joinToString("&")

                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = if (responseCode == 200) {
                    connection.inputStream.bufferedReader().readText()
                } else {
                    connection.errorStream?.bufferedReader()?.readText() ?: "Unknown error"
                }

                android.util.Log.d("Account", "Response code: $responseCode")
                android.util.Log.d("Account", "Response: $response")

                withContext(Dispatchers.Main) {
                    try {
                        val jsonResponse = JSONObject(response)
                        if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                            Toast.makeText(
                                this@Account,
                                "Account updated successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                            hasImageChanged = false
                        } else {
                            val message = jsonResponse.optString("message", "Failed to update account")
                            Toast.makeText(this@Account, message, Toast.LENGTH_LONG).show()
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("Account", "JSON parse error", e)
                        Toast.makeText(
                            this@Account,
                            "Server response error: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("Account", "Network error", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error saving changes: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showDeleteConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to permanently delete your account? This action cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                deleteAccount()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun deleteAccount() {
        val userId = sessionManager.getUserId()

        if (userId == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val url = URL(ApiConfig.DELETE_ACCOUNT_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "POST"
                connection.doOutput = true

                val postData = "user_id=$userId"
                OutputStreamWriter(connection.outputStream).use { it.write(postData) }

                val responseCode = connection.responseCode
                val response = connection.inputStream.bufferedReader().readText()
                val jsonResponse = JSONObject(response)

                withContext(Dispatchers.Main) {
                    if (responseCode == 200 && jsonResponse.getString("status") == "success") {
                        sessionManager.logoutUser()
                        Toast.makeText(
                            this@Account,
                            "Account deleted successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val intent = Intent(this@Account, Login::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    } else {
                        Toast.makeText(
                            this@Account,
                            jsonResponse.getString("message"),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@Account,
                        "Error deleting account: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun applyTheme() {
        val prefs = getSharedPreferences("ThemePrefs", Context.MODE_PRIVATE)
        val isDarkMode = prefs.getBoolean("isDarkMode", false)

        val rootLayout = findViewById<LinearLayout>(R.id.rootLayout)
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
