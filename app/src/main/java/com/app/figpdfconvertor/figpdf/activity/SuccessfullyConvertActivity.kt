package com.app.figpdfconvertor.figpdf.activity

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivitySuccessfullyConvertBinding
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import java.io.File

class SuccessfullyConvertActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySuccessfullyConvertBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivitySuccessfullyConvertBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val pdfPath = intent.getStringExtra("pdf_path")
        val pdfName = intent.getStringExtra("pdf_name")
        setupBackPressed()
        binding.txtPdfName.text = pdfName
        binding.imgBack.setOnClickListener {
            finish()
        }
//        binding.txtPdfPath.text = pdfPath


        // Generate PDF thumbnail
        if (pdfPath != null) {
            val thumbnail = getPdfThumbnail(pdfPath)
            if (thumbnail != null) binding.imgPdfThumbnail.setImageBitmap(thumbnail)
        }

        binding.rlPdfOpen.setOnClickListener {
            val intent = Intent(this, PDFViewerActivity::class.java)
            intent.putExtra("pdf_path", pdfPath) // send path
            startActivity(intent)
        }

        binding.rlShare.setOnClickListener {
            val pdfPath = intent.getStringExtra("pdf_path")
            if (pdfPath != null) {
                val file = File(pdfPath)
                if (file.exists()) {
                    val uri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        "${packageName}.provider",
                        file
                    )

                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                        type = "application/pdf"
                        putExtra(Intent.EXTRA_STREAM, uri)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }

                    startActivity(Intent.createChooser(shareIntent, "Share PDF via"))
                } else {
                    Toast.makeText(this, "File does not exist", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getPdfThumbnail(pdfPath: String): Bitmap? {
        return try {
            val file = File(pdfPath)
            if (!file.exists()) return null

            // Use PdfRenderer for Android Lollipop+
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                val parcelFileDescriptor =
                    ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                val renderer = android.graphics.pdf.PdfRenderer(parcelFileDescriptor)
                if (renderer.pageCount > 0) {
                    val page = renderer.openPage(0)
                    val bitmap =
                        Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(
                        bitmap,
                        null,
                        null,
                        android.graphics.pdf.PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY
                    )
                    page.close()
                    renderer.close()
                    parcelFileDescriptor.close()
                    bitmap
                } else null
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun setupBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goToMainActivity()
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun goToMainActivity() {
        finish()
    }
}