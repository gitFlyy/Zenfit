package com.example.zenfit

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream

class UploadPost : AppCompatActivity() {

    private lateinit var imagePreview: ImageView
    private lateinit var selectImageText: TextView
    private lateinit var btnSelectImage: Button
    private lateinit var btnUpload: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var statusText: TextView
    private lateinit var sessionManager: SessionManager

    private var selectedImageUri: Uri? = null
    private var imageBase64: String? = null

    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_upload_post)

        sessionManager = SessionManager(this)

        // Initialize views
        imagePreview = findViewById(R.id.imagePreview)
        selectImageText = findViewById(R.id.selectImageText)
        btnSelectImage = findViewById(R.id.btnSelectImage)
        btnUpload = findViewById(R.id.btnUpload)
        progressBar = findViewById(R.id.progressBar)
        statusText = findViewById(R.id.statusText)

        val backArrow = findViewById<ImageView>(R.id.backArrow)
        backArrow.setOnClickListener {
            finish()
        }

        // Select image button
        btnSelectImage.setOnClickListener {
            openImagePicker()
        }

        // Image preview card click
        findViewById<androidx.cardview.widget.CardView>(R.id.imagePreviewCard).setOnClickListener {
            openImagePicker()
        }

        // Upload button
        btnUpload.setOnClickListener {
            uploadPost()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        @Suppress("DEPRECATION")
        startActivityForResult(intent, PICK_IMAGE_REQUEST)
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            selectedImageUri = data.data
            selectedImageUri?.let { uri ->
                try {
                    // Display image preview
                    imagePreview.setImageURI(uri)
                    selectImageText.visibility = View.GONE

                    // Convert to base64
                    val bitmap = getBitmapFromUri(uri)
                    bitmap?.let {
                        imageBase64 = convertBitmapToBase64(it)
                        btnUpload.isEnabled = true
                        btnUpload.backgroundTintList = android.content.res.ColorStateList.valueOf(
                            android.graphics.Color.parseColor("#FF6B35")
                        )
                        statusText.visibility = View.GONE
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getBitmapFromUri(uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Resize bitmap if too large (to reduce memory usage)
            if (bitmap.width > 1024 || bitmap.height > 1024) {
                val ratio = Math.min(1024.0 / bitmap.width, 1024.0 / bitmap.height)
                val width = (bitmap.width * ratio).toInt()
                val height = (bitmap.height * ratio).toInt()
                Bitmap.createScaledBitmap(bitmap, width, height, true)
            } else {
                bitmap
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
        return "$base64String"
    }

    private fun uploadPost() {
        if (imageBase64 == null) {
            Toast.makeText(this, "Please select an image first", Toast.LENGTH_SHORT).show()
            return
        }

        val userId = sessionManager.getUserId()
        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading
        progressBar.visibility = View.VISIBLE
        btnUpload.isEnabled = false
        btnSelectImage.isEnabled = false
        statusText.text = "Uploading..."
        statusText.visibility = View.VISIBLE

        val url = ApiConfig.UPLOAD_POST_URL

        val request = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        progressBar.visibility = View.GONE
                        statusText.text = "Post uploaded successfully!"
                        statusText.setTextColor(android.graphics.Color.parseColor("#4CAF50"))

                        Toast.makeText(this, "Post uploaded successfully!", Toast.LENGTH_SHORT).show()

                        // Return to previous screen after a short delay
                        imagePreview.postDelayed({
                            finish()
                        }, 1500)
                    } else {
                        val message = json.optString("message", "Upload failed")
                        showError(message)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    showError("Error parsing response")
                }
            },
            { error ->
                error.printStackTrace()
                showError("Network error: ${error.message}")
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["image_data"] = imageBase64!!
                return params
            }

            override fun getHeaders(): MutableMap<String, String> {
                val headers = HashMap<String, String>()
                headers["Content-Type"] = "application/x-www-form-urlencoded"
                return headers
            }
        }

        // Increase timeout for large images
        request.setRetryPolicy(
            com.android.volley.DefaultRetryPolicy(
                30000,
                com.android.volley.DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                com.android.volley.DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        )

        Volley.newRequestQueue(this).add(request)
    }

    private fun showError(message: String) {
        progressBar.visibility = View.GONE
        statusText.text = message
        statusText.setTextColor(android.graphics.Color.parseColor("#F44336"))
        statusText.visibility = View.VISIBLE
        btnUpload.isEnabled = true
        btnSelectImage.isEnabled = true
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
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
