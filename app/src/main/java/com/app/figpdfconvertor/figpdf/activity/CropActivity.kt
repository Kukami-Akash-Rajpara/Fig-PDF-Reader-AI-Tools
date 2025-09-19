//package com.app.figpdfconvertor.figpdf.activity
//
//import android.content.Intent
//import android.graphics.Bitmap
//import android.net.Uri
//import android.os.Bundle
//import android.widget.Toast
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import com.app.figpdfconvertor.figpdf.R
//import com.app.figpdfconvertor.figpdf.activity.BaseActivity
//import com.app.figpdfconvertor.figpdf.databinding.ActivityCropBinding
//import com.app.figpdfconvertor.figpdf.utils.MyUtils
//import java.io.File
//import java.io.FileOutputStream
//
//class CropActivity : BaseActivity() {
//
//    companion object {
//        const val EXTRA_INPUT_URI = "input_uri"
//        const val EXTRA_OUTPUT_URI = "output_uri"
//    }
//
//    private lateinit var binding: ActivityCropBinding
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        MyUtils.fullScreenLightStatusBar(this)
//        binding = ActivityCropBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        val input = intent.getStringExtra(EXTRA_INPUT_URI)
//        if (input.isNullOrEmpty()) {
//            Toast.makeText(this, "No image", Toast.LENGTH_SHORT).show()
//            finish()
//            return
//        }
//
//        // Load image
//        binding.cropImageView.setImageUriAsync(Uri.parse(input))
//
//        // Aspect ratio buttons
//        binding.btnFree.setOnClickListener {
//            binding.cropImageView.clearAspectRatio()
//        }
//        binding.btn11.setOnClickListener {
//            binding.cropImageView.setAspectRatio(1, 1)
//        }
//        binding.btn45.setOnClickListener {
//            binding.cropImageView.setAspectRatio(4, 5)
//        }
//        binding.btn169.setOnClickListener {
//            binding.cropImageView.setAspectRatio(16, 9)
//        }
//
//        // Rotate button
//        binding.btnRotate.setOnClickListener {
//            binding.cropImageView.rotateImage(90)
//        }
//
//        // Reset button
//        binding.btnReset.setOnClickListener {
//            binding.cropImageView.resetCropRect()
//        }
//
//        // Done button
//        binding.btnDone.setOnClickListener {
//            val cropped: Bitmap? = binding.cropImageView.croppedImage
//            if (cropped == null) {
//                Toast.makeText(this, "Failed to crop", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }
//            val out = saveToCache(cropped)
//            setResult(RESULT_OK, Intent().putExtra(EXTRA_OUTPUT_URI, out.toString()))
//            finish()
//        }
//
//        // Back button
//        binding.imgBack.setOnClickListener { finish() }
//    }
//
//    private fun saveToCache(bmp: Bitmap): Uri {
//        val file = File(cacheDir, "crop_${System.currentTimeMillis()}.jpg")
//        FileOutputStream(file).use {
//            bmp.compress(Bitmap.CompressFormat.JPEG, 95, it)
//        }
//        return Uri.fromFile(file)
//    }
//}