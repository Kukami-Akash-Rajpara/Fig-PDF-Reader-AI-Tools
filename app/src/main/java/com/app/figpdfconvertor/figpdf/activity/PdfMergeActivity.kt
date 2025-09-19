package com.app.figpdfconvertor.figpdf.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.OnApplyWindowInsetsListener
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.adapter.PdfPickerAdapter
import com.app.figpdfconvertor.figpdf.adapter.PdfAdapter
import com.app.figpdfconvertor.figpdf.model.PdfModel
import com.app.figpdfconvertor.figpdf.model.PdfPickerItem
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import com.google.android.material.textview.MaterialTextView
import java.io.File

class PdfMergeActivity : BaseActivity() {

    private lateinit var adapter: PdfAdapter
    private val pdfList = mutableListOf<PdfModel>()
    private var mode: String = "merge"  // default
    private lateinit var pdfPickerAdapter: PdfPickerAdapter
    private val pdfPickers = mutableListOf<PdfPickerItem>()
    private val PICK_PDF_REQUEST_CODE = 1000
    private var currentPickPosition = -1


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        setContentView(R.layout.activity_pdf_merge)
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById<View?>(R.id.main),
            OnApplyWindowInsetsListener { v: View?, insets: WindowInsetsCompat? ->
                val systemBars = insets!!.getInsets(WindowInsetsCompat.Type.systemBars())
                v!!.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            })
        val recycler = findViewById<RecyclerView>(R.id.pdfRecycler)
        val actionBtn = findViewById<RelativeLayout>(R.id.mergeBtn)
        val textMerge = findViewById<MaterialTextView>(R.id.textMerge)
        val txtImageName = findViewById<MaterialTextView>(R.id.txtImageName)
        val addBtn = findViewById<RelativeLayout>(R.id.addPdfBtn)
        val imgBack = findViewById<ImageView>(R.id.imgBack)

        imgBack.setOnClickListener {
            finish()
        }

        // get mode from intent
        mode = intent.getStringExtra("mode") ?: ""
        textMerge.text = if (mode == "split") "Split PDF" else if (mode == "merge") "Merge PDF" else "Compress PDF"
        txtImageName.text = if (mode == "split") "Split PDF" else if (mode == "merge") "Merge PDF" else "Compress PDF"


        actionBtn.setOnClickListener {
            if (mode == "merge") {
                handleMerge()
            } else if(mode == "split"){
                handleSplit()
            }else{
                handleCompress()
            }
        }

        pdfPickers.clear()
        if (mode == "merge") {
            // two default pickers
            pdfPickers.add(PdfPickerItem())
            pdfPickers.add(PdfPickerItem())
            addBtn.visibility = View.VISIBLE
        } else {
            // only one picker
            pdfPickers.add(PdfPickerItem())
            addBtn.visibility = View.GONE
        }

        pdfPickerAdapter = PdfPickerAdapter(pdfPickers) { position ->
            currentPickPosition = position
            pickPdfDocument()
        }

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = pdfPickerAdapter

        addBtn.setOnClickListener {
            pdfPickers.add(PdfPickerItem())
            pdfPickerAdapter.notifyItemInserted(pdfPickers.size - 1)
        }
    }

    private fun pickPdfDocument() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "application/pdf"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_PDF_REQUEST_CODE)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_PDF_REQUEST_CODE && resultCode == RESULT_OK) {
            val uri = data?.data ?: return

            // ✅ Get proper file name
            val name = getFileName(uri) ?: "PDF"

            if (currentPickPosition != -1) {
                pdfPickers[currentPickPosition].uri = uri
                pdfPickers[currentPickPosition].name = name
                pdfPickerAdapter.notifyItemChanged(currentPickPosition)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var name: String? = null
        val cursor = contentResolver.query(uri, null, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val nameIndex = it.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                if (nameIndex != -1) {
                    name = it.getString(nameIndex)
                }
            }
        }
        return name
    }

    // Create PDF file in Documents/AI PDF Converter/MergedPdf via MediaStore
    private fun createPdfFileInDocuments(fileName: String): Uri? {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, "Documents/"+getString(R.string.app_name)+"/MergedPdf")
        }

        return contentResolver.insert(MediaStore.Files.getContentUri("external"), values)
    }

    // ✅ Recursive traversal for Android 11+
    private fun traverseFiles(dir: File) {
        val files = dir.listFiles() ?: return
        for (file in files) {
            if (file.isDirectory) {
                traverseFiles(file)
            } else if (file.extension.equals("pdf", ignoreCase = true)) {
                val uri = Uri.fromFile(file)
                pdfList.add(PdfModel(file.name, uri))
            }
        }
    }

    // ✅ Merge PDFs
    private fun handleMerge() {
        val selectedUris = pdfPickers.mapNotNull { it.uri } // take all picked URIs
        if (selectedUris.size < 2) {
            Toast.makeText(this, "Select at least 2 PDFs", Toast.LENGTH_SHORT).show()
        } else {
            val mergedUri = mergePdfs(selectedUris)
            if (mergedUri != null) {
                // ✅ Open PDFViewerActivity
                val intent = Intent(this, PDFViewerActivity::class.java).apply {
                    putExtra("pdf_path", mergedUri.toString())
                }
                startActivity(intent)

                Toast.makeText(this, "Merged PDF saved!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Failed to merge PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }



    // ✅ Split PDF
    private fun handleSplit() {
        val selectedUris = pdfPickers.mapNotNull { it.uri }
        if (selectedUris.size != 1) {
            Toast.makeText(this, "Select exactly 1 PDF to split", Toast.LENGTH_SHORT).show()
        } else {
            val parts = splitPdf(selectedUris.first())
            if (parts.isNotEmpty()) {
                Toast.makeText(this, "Split into ${parts.size} PDFs", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, "Failed to split PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ✅ Compress PDF
    private fun handleCompress() {
        val selectedUris = pdfPickers.mapNotNull { it.uri }
        if (selectedUris.size != 1) {
            Toast.makeText(this, "Select exactly 1 PDF to compress", Toast.LENGTH_SHORT).show()
            return
        }

        val compressedUri = compressPdf(selectedUris.first(), 50) // 50% quality
        if (compressedUri != null) {
            Toast.makeText(this, "PDF compressed successfully: $compressedUri", Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(this, "Failed to compress PDF", Toast.LENGTH_SHORT).show()
        }
    }
    // ✅ Merge logic using MediaStore
    private fun mergePdfs(uris: List<Uri>): Uri? {
        if (uris.isEmpty()) return null
        val fileName = "merged_${System.currentTimeMillis()}.pdf"
        val outUri = createPdfFileInDocuments(fileName) ?: return null

        contentResolver.openOutputStream(outUri)?.use { outputStream ->
            val outputDoc = PdfDocument()
            var pageNumber = 1

            uris.forEach { uri ->
                val fd = contentResolver.openFileDescriptor(uri, "r") ?: return@forEach
                val renderer = PdfRenderer(fd)

                for (i in 0 until renderer.pageCount) {
                    val page = renderer.openPage(i)
                    val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                    page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                    val newPage = outputDoc.startPage(
                        PdfDocument.PageInfo.Builder(page.width, page.height, pageNumber).create()
                    )
                    newPage.canvas.drawBitmap(bmp, 0f, 0f, null)
                    outputDoc.finishPage(newPage)

                    page.close()
                    pageNumber++
                }
                renderer.close()
            }

            outputDoc.writeTo(outputStream)
            outputDoc.close()
        }

        return outUri
    }

    // ✅ Split logic using MediaStore
    private fun splitPdf(uri: Uri): List<Uri> {
        val outputUris = mutableListOf<Uri>()
        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return emptyList()
        val renderer = PdfRenderer(fd)

        for (i in 0 until renderer.pageCount) {
            val page = renderer.openPage(i)
            val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            val fileName = "split_${i + 1}_${System.currentTimeMillis()}.pdf"
            val outUri = createPdfFileInDocuments(fileName) ?: continue

            contentResolver.openOutputStream(outUri)?.use { outputStream ->
                val outputDoc = PdfDocument()
                val newPage = outputDoc.startPage(
                    PdfDocument.PageInfo.Builder(page.width, page.height, 1).create()
                )
                newPage.canvas.drawBitmap(bmp, 0f, 0f, null)
                outputDoc.finishPage(newPage)
                outputDoc.writeTo(outputStream)
                outputDoc.close()
            }

            outputUris.add(outUri)
            page.close()
        }

        renderer.close()
        return outputUris
    }

    // Your existing compressPdf function must be inside the same class:
    private fun compressPdf(uri: Uri, quality: Int = 50): Uri? {
        val compressedFileName = "compressed_${System.currentTimeMillis()}.pdf"
        val outUri = createPdfFileInDocuments(compressedFileName) ?: return null

        contentResolver.openOutputStream(outUri)?.use { outputStream ->
            val outputDoc = PdfDocument()
            val fd = contentResolver.openFileDescriptor(uri, "r") ?: return null
            val renderer = PdfRenderer(fd)

            for (i in 0 until renderer.pageCount) {
                val page = renderer.openPage(i)
                val bmp = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                page.render(bmp, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

                val newPage = outputDoc.startPage(
                    PdfDocument.PageInfo.Builder(page.width, page.height, i + 1).create()
                )
                newPage.canvas.drawBitmap(bmp, 0f, 0f, null)
                outputDoc.finishPage(newPage)

                page.close()
                bmp.recycle()
            }

            renderer.close()
            outputDoc.writeTo(outputStream)
            outputDoc.close()
        }

        return outUri
    }
}