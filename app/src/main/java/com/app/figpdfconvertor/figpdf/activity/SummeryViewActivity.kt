package com.app.figpdfconvertor.figpdf.activity

import android.content.ClipData
import android.content.ClipboardManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivitySummeryViewBinding
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import java.io.File

class SummeryViewActivity : BaseActivity() {

    private lateinit var binding: ActivitySummeryViewBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivitySummeryViewBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val txtPath = intent.getStringExtra("txt_path")
        if (txtPath != null) {
            val file = File(txtPath)
            if (file.exists()) {
                try {
                    val content = file.readText()
                    binding.txtSummary.text = content
                } catch (e: Exception) {
                    e.printStackTrace()
                    binding.txtSummary.text = "Unable to load content"
                }
            } else {
                binding.txtSummary.text = "File not found"
            }
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        // Copy button functionality
        binding.imgCopy.setOnClickListener {
            val textToCopy = binding.txtSummary.text.toString()
            val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("TXT Content", textToCopy)
            clipboard.setPrimaryClip(clip)
            Toast.makeText(this, "Text copied to clipboard", Toast.LENGTH_SHORT).show()
        }
    }
}