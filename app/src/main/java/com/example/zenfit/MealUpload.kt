package com.example.zenfit

import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MealUpload : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private lateinit var cameraIcon: ImageView
    private var selectedImageUri: Uri? = null
    private var currentPhotoPath: String = ""

    // Modern activity result launchers
    private val cameraLauncher = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            selectedImageUri = Uri.fromFile(File(currentPhotoPath))
            displaySelectedImage()
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            displaySelectedImage()
        }
    }

    private val cameraPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openCamera()
        } else {
            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    private val storagePermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            openGallery()
        } else {
            Toast.makeText(this, "Storage permission denied", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_meal_upload)

        sessionManager = SessionManager(this)

        val backButton = findViewById<ImageButton>(R.id.back_button)
        val takePhotoButton = findViewById<Button>(R.id.btn_take_photo)
        cameraIcon = findViewById<ImageView>(R.id.camera_icon)
        val etName = findViewById<EditText>(R.id.et_meal_name)
        val etCalories = findViewById<EditText>(R.id.et_calories)
        val etCarbs = findViewById<EditText>(R.id.et_carbs)
        val etProtein = findViewById<EditText>(R.id.et_protein)
        val btnUpload = findViewById<Button>(R.id.btn_upload_meal)

        backButton.setOnClickListener {
            finish()
        }

        takePhotoButton.setOnClickListener {
            showImagePickerDialog()
        }

        btnUpload.setOnClickListener {
            val name = etName.text.toString().trim()
            val calories = etCalories.text.toString().trim()
            val carbs = etCarbs.text.toString().trim()
            val protein = etProtein.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Please enter meal name"
                etName.requestFocus()
                return@setOnClickListener
            }

            if (calories.isEmpty()) {
                etCalories.error = "Please enter calories"
                etCalories.requestFocus()
                return@setOnClickListener
            }

            uploadMeal(name, calories, carbs, protein)
        }
    }

    private fun showImagePickerDialog() {
        val options = arrayOf("Take Photo", "Choose from Gallery", "Cancel")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Image")
        builder.setItems(options) { dialog, which ->
            when (which) {
                0 -> checkCameraPermissionAndOpen()
                1 -> checkStoragePermissionAndOpen()
                2 -> dialog.dismiss()
            }
        }
        builder.show()
    }

    private fun checkCameraPermissionAndOpen() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }
            else -> {
                cameraPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }

    private fun checkStoragePermissionAndOpen() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ uses READ_MEDIA_IMAGES
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_MEDIA_IMAGES
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(android.Manifest.permission.READ_MEDIA_IMAGES)
                }
            }
        } else {
            // Below Android 13 uses READ_EXTERNAL_STORAGE
            when {
                ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    openGallery()
                }
                else -> {
                    storagePermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun openCamera() {
        try {
            val photoFile = createImageFile()
            val photoURI = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                photoFile
            )
            cameraLauncher.launch(photoURI)
        } catch (ex: IOException) {
            Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(android.os.Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun displaySelectedImage() {
        try {
            selectedImageUri?.let { uri ->
                val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                cameraIcon.setImageBitmap(bitmap)
                cameraIcon.scaleType = ImageView.ScaleType.CENTER_CROP
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uploadMeal(name: String, calories: String, carbs: String, protein: String) {
        val userId = sessionManager.getUserId() ?: ""
        val url = ApiConfig.UPLOAD_MEAL_URL

        val progress = ProgressDialog(this)
        progress.setMessage("Uploading meal...")
        progress.setCancelable(false)
        progress.show()

        val request = object : StringRequest(
            Request.Method.POST, url,
            { response ->
                progress.dismiss()
                try {
                    // Log the raw response for debugging
                    android.util.Log.d("MealUpload", "Server response: $response")

                    val json = JSONObject(response)
                    if (json.getBoolean("success")) {
                        Toast.makeText(this, json.optString("message", "Uploaded"), Toast.LENGTH_LONG).show()
                        finish()
                    } else {
                        Toast.makeText(this, json.optString("message", "Upload failed"), Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    android.util.Log.e("MealUpload", "Parse error: ${e.message}")
                    android.util.Log.e("MealUpload", "Raw response: $response")
                    Toast.makeText(this, "Invalid server response: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                progress.dismiss()
                android.util.Log.e("MealUpload", "Network error: ${error.message}")
                Toast.makeText(this, "Upload failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): MutableMap<String, String> {
                val params = HashMap<String, String>()
                params["user_id"] = userId
                params["name"] = name
                params["calories"] = calories
                params["carbs"] = carbs
                params["protein"] = protein

                // Log params for debugging
                android.util.Log.d("MealUpload", "Sending params: user_id=$userId, name=$name, calories=$calories")

                // Convert image to base64 if selected
                selectedImageUri?.let { uri ->
                    try {
                        val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
                        val resizedBitmap = resizeBitmap(bitmap, 800, 800)
                        val byteArrayOutputStream = ByteArrayOutputStream()
                        resizedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream)
                        val imageBytes = byteArrayOutputStream.toByteArray()
                        val base64Image = Base64.encodeToString(imageBytes, Base64.DEFAULT)
                        params["image"] = base64Image
                        android.util.Log.d("MealUpload", "Image encoded, length: ${base64Image.length}")
                    } catch (e: Exception) {
                        e.printStackTrace()
                        android.util.Log.e("MealUpload", "Image encoding error: ${e.message}")
                    }
                }

                return params
            }
        }

        Volley.newRequestQueue(this).add(request)
    }

    private fun resizeBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val ratioBitmap = width.toFloat() / height.toFloat()
        val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()

        var finalWidth = maxWidth
        var finalHeight = maxHeight

        if (ratioMax > ratioBitmap) {
            finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
        } else {
            finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
        }

        return Bitmap.createScaledBitmap(bitmap, finalWidth, finalHeight, true)
    }
}
