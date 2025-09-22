package com.app.figpdfconvertor.figpdf.activity

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import com.app.figpdfconvertor.figpdf.databinding.ActivityPptMainBinding

class PptMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPptMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPptMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())
    }
}