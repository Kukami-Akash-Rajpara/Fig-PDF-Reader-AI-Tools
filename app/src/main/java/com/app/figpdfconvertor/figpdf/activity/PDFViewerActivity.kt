package com.app.figpdfconvertor.figpdf.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bhuvaneshw.pdf.PdfViewer
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivityPdfviewerBinding
import com.app.figpdfconvertor.figpdf.utils.MyUtils

class PDFViewerActivity : BaseActivity() {

    private lateinit var binding: ActivityPdfviewerBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityPdfviewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val pdfPath = intent.getStringExtra("pdf_path") ?: return
        val pdfViewer = findViewById<PdfViewer>(R.id.pdfViewer)

        binding.imgBack.setOnClickListener {
            finish()
        }
        // Load local PDF file path
        pdfViewer.onReady {
            load(pdfPath)
        }
    }
}