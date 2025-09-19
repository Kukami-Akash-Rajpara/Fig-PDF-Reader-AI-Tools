package com.app.figpdfconvertor.figpdf.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.createBitmap
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivityPdfToImagesBinding
import com.app.figpdfconvertor.figpdf.adapter.PdfImageAdapter
import com.app.figpdfconvertor.figpdf.utils.MyUtils

class PdfToImagesActivity : BaseActivity() {

    private val images = mutableListOf<Bitmap>()
    private lateinit var adapter: PdfImageAdapter

    private var pdfUri: Uri? = null
    private var binding: ActivityPdfToImagesBinding? = null
    companion object {
        const val PICK_PDF = 1001
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityPdfToImagesBinding.inflate(getLayoutInflater())
        setContentView(binding!!.getRoot())
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        adapter = PdfImageAdapter(images)
        binding!!.recyclerPdfImages.layoutManager = GridLayoutManager(this, 2) // 2 columns
        binding!!.recyclerPdfImages.adapter = adapter
        pdfUri = intent.getParcelableExtra("extra_file_uri", Uri::class.java)
        if (pdfUri != null) {
            renderPdfToImages(pdfUri!!)
        }

        binding!!.btnSaveImages.setOnClickListener {
            saveAllImagesToGallery()
        }

        binding!!.imgBack.setOnClickListener {
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF && resultCode == RESULT_OK) {
            pdfUri = data?.data
            if (pdfUri != null) {
                renderPdfToImages(pdfUri!!)
            }
        }
    }

    private fun renderPdfToImages(uri: Uri) {
        images.clear()
        val fileDescriptor: ParcelFileDescriptor? =
            contentResolver.openFileDescriptor(uri, "r")
        if (fileDescriptor != null) {
            val renderer = PdfRenderer(fileDescriptor)
            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bitmap = createBitmap(page.width, page.height)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                images.add(bitmap)
                page.close()
            }
            renderer.close()
            fileDescriptor.close()
            adapter.notifyDataSetChanged()
        }
    }

    private fun saveAllImagesToGallery() {
        if (images.isEmpty()) {
            Toast.makeText(this, "No images to save", Toast.LENGTH_SHORT).show()
            return
        }

        for ((index, bitmap) in images.withIndex()) {
            val fileName = "pdf_page_${index + 1}.png"
            val savedUri = saveImageToGallery(bitmap, fileName)
            if (savedUri != null) {
                Toast.makeText(this, "Saved $fileName", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveImageToGallery(bitmap: Bitmap, fileName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PDFImages")
        }

        val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { out ->
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
            }
        }
        return uri
    }
}
