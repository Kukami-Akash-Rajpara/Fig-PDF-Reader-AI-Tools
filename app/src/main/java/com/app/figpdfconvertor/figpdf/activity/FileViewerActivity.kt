package com.app.figpdfconvertor.figpdf.activity

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.ads.AdManagerInter
import com.app.figpdfconvertor.figpdf.databinding.ActivityFileViewerBinding
import com.app.figpdfconvertor.figpdf.preferences.AppHelper
import com.app.figpdfconvertor.figpdf.utils.MyApp
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import com.cherry.lib.doc.bean.DocSourceType
import com.cherry.lib.doc.office.fc.ss.usermodel.Workbook
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.itextpdf.kernel.pdf.PdfReader
import com.itextpdf.kernel.pdf.canvas.parser.PdfTextExtractor
import com.itextpdf.kernel.pdf.canvas.parser.listener.SimpleTextExtractionStrategy
import org.apache.poi.hslf.usermodel.HSLFSlideShow
import org.apache.poi.hslf.usermodel.HSLFTextShape
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.hwpf.HWPFDocument
import org.apache.poi.util.Units
import org.apache.poi.xslf.usermodel.XMLSlideShow
import org.apache.poi.xslf.usermodel.XSLFTextShape
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

class FileViewerActivity : BaseActivity() {

    companion object {
        const val EXTRA_FILE_URI = "extra_file_uri"
        const val EXTRA_FILE_TYPE = "extra_file_type"
        const val EXTRA_CONVERSION_MODE = "extra_conversion_mode"

        // Conversion modes
        const val MODE_TO_PDF = "TO_PDF"
        const val MODE_FROM_PDF = "FROM_PDF"

        // File types
        const val FILE_TYPE_WORD = "WORD"
        const val FILE_TYPE_EXCEL = "EXCEL"
        const val FILE_TYPE_PPT = "PPT"
        const val FILE_TYPE_TEXT = "TEXT"
        const val FILE_TYPE_HTML = "HTML"
        const val FILE_TYPE_PDF = "PDF"
    }

    private lateinit var binding: ActivityFileViewerBinding
    private var fileUri: Uri? = null
    private var fileType: String? = null
    private var conversionMode: String? = null
    private var pdfConversionType: String? = null

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityFileViewerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        conversionMode = intent.getStringExtra(EXTRA_CONVERSION_MODE) ?: ""
        pdfConversionType = intent.getStringExtra("conversion_type")

        fileUri = intent.getParcelableExtra(EXTRA_FILE_URI, Uri::class.java)
        fileType = intent.getStringExtra(EXTRA_FILE_TYPE)

        binding.imgBack.setOnClickListener {
            finish()
        }

        if (fileUri == null || fileType == null) {
            Toast.makeText(this, "Invalid file", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        //binding.txtButton.text = "PDF to " + (pdfConversionType ?: fileType ?: "File")

        if (conversionMode == MODE_FROM_PDF) {
            binding.txtButton.text = "PDF to ${pdfConversionType ?: "File"}"
        } else if (conversionMode == MODE_TO_PDF) {
            binding.txtButton.text = "${fileType ?: "File"} to PDF"
        }

        binding.webViewFile.openDoc(
            this,
            fileUri.toString(),
            DocSourceType.URI
        )

        binding.btnConvert.setOnClickListener {
            val timeStamp = System.currentTimeMillis()

            if (conversionMode == MODE_FROM_PDF && fileUri != null) {
                // ---------------- PDF → Other formats ----------------
                when (pdfConversionType) {
                    "WORD" -> {
                        val bytes = convertPdfWithOcrToWord(fileUri!!)
                        val path = saveFileToDocuments("Converted_$timeStamp.docx", "WordFromPdf", bytes)
                        Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
                    }
                    "EXCEL" -> {
                        val bytes = convertPdfToExcelBytes(fileUri!!)
                        val path = saveFileToDocuments("Converted_$timeStamp.xlsx", "ExcelFromPdf", bytes)
                        Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
                    }
                    "PPT" -> {
                        val bytes = convertPdfToPptBytes(fileUri!!)
                        val path = saveFileToDocuments("Converted_$timeStamp.pptx", "PptFromPdf", bytes)
                        Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
                    }
                    "TEXT" -> {
                        val text = convertPdfToText(fileUri!!)
                        val path = saveFileToDocuments("Converted_$timeStamp.txt", "TextFromPdf", text.toByteArray())
                        Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
                    }
                    "HTML" -> {
                        val bytes = convertPdfToHtmlBytes(fileUri!!)
                        val path = saveFileToDocuments("Converted_$timeStamp.html", "HtmlFromPdf", bytes)
                        Toast.makeText(this, "Saved: $path", Toast.LENGTH_LONG).show()
                    }
                    else -> Toast.makeText(this, "Unknown conversion type", Toast.LENGTH_SHORT).show()
                }

            } else if (conversionMode == MODE_TO_PDF && fileUri != null && fileType != null) {
                // ---------------- Other formats → PDF ----------------
                convertFileToPdf(fileUri!!, fileType!!)
            }
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val idx = cursor.getColumnIndex(MediaStore.MediaColumns.DISPLAY_NAME)
                    if (idx != -1) {
                        result = cursor.getString(idx)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/') ?: -1
            if (cut != -1) {
                result = result?.substring(cut + 1)
            }
        }
        return result
    }

    /** ---------------------- Main Conversion ---------------------- **/
    private fun convertFileToPdf(uri: Uri, type: String) {
        try {
            val outputFileName = "converted_${System.currentTimeMillis()}.pdf"
            when (type) {
                FILE_TYPE_TEXT, FILE_TYPE_HTML -> {
                    val text = contentResolver.openInputStream(uri)?.bufferedReader()
                        ?.use { it.readText() } ?: ""
                    val pdfBytes = convertTextToPdfBytes(text)
                    saveFileToDownloads(outputFileName, pdfBytes,false,false)
                }

                FILE_TYPE_WORD -> {
                    val fileName = getFileName(uri) ?: "document.docx"
                    val pdfBytes = convertWordToPdfBytes(uri, fileName)
                    saveFileToDownloads(outputFileName, pdfBytes,true,false)
                }

                FILE_TYPE_EXCEL -> {
                    val fileName = getFileName(uri) ?: "workbook.xlsx"
                    val pdfBytes = convertExcelToPdfBytes(uri, fileName)
                    saveFileToDownloads(outputFileName, pdfBytes,false,false)
                }

                FILE_TYPE_PPT -> {
                    val fileName = getFileName(uri) ?: "presentation.pptx"
                    if (!fileName.endsWith(".pptx", true)) {
                        Toast.makeText(
                            this,
                            "Only PPTX files are supported on Android",
                            Toast.LENGTH_SHORT
                        ).show()
                        return
                    }
                    val pdfBytes = convertPptToPdfBytes(uri, fileName)
                    saveFileToDownloads(outputFileName, pdfBytes,false,true)
                }

                else -> {
                    Toast.makeText(this, "Unsupported file type: $type", Toast.LENGTH_SHORT).show()
                    return
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Conversion failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun goToDownloadScreen(file: File?) {
        Log.e("PATHHH",Uri.fromFile(file).toString())
        val intent = Intent(this, ViewFileActivity::class.java)
        intent.putExtra("pdf_uri", file?.absolutePath)
        startActivity(intent)
    }
    /** ---------------------- Save PDF ---------------------- **/
    private fun saveFileToDownloads(fileName: String, data: ByteArray, isWord: Boolean,isPpt: Boolean) {
       /* try {
            val resolver = contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/MyConvertedFiles")
            }
            val uri = resolver.insert(MediaStore.Files.getContentUri("external"), contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { it.write(data) }
                Toast.makeText(
                    this,
                    "Saved to Downloads/MyConvertedFiles/$fileName",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                Toast.makeText(this, "Failed to save file", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }*/
        val subFolder = "ToPDF"
        try {
            val appName = getString(R.string.app_name)
            val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
            val appDir = File(documentsDir, "$appName/$subFolder")

            if (!appDir.exists()) {
                appDir.mkdirs()
            }

            val file = File(appDir, fileName)
            FileOutputStream(file).use { it.write(data) }

            Toast.makeText(
                this,
                "Saved to ${appDir.absolutePath}/$fileName",
                Toast.LENGTH_LONG
            ).show()

            // ✅ Optional: Open ViewFileActivity immediately

            if (isWord){
                if (AppHelper.getShowInterWordToPdf()) {
                    AdManagerInter.renderInterAdFixed(this@FileViewerActivity, {
                        goToDownloadScreen(file);
                    })
                } else {
                    goToDownloadScreen(file)
                }
            }else if (isPpt){
                if (AppHelper.getShowInterPptToPdf()) {
                    AdManagerInter.renderInterAdFixed(this@FileViewerActivity, {
                        goToDownloadScreen(file)
                    })
                } else {
                    goToDownloadScreen(file)
                }
            }else{
                goToDownloadScreen(file)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error saving file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    /** ---------------------- TEXT / HTML ---------------------- **/
    private fun convertTextToPdfBytes(text: String): ByteArray {
        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val margin = 50f

        val paint = Paint().apply { textSize = 14f }
        val lineHeight = paint.descent() - paint.ascent()

        var y = margin
        val usableWidth = pageWidth - 2 * margin
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        for (line in text.split("\n")) {
            var currentLine = line

            while (currentLine.isNotEmpty()) {
                // Check if line fits in width
                val charsThatFit = paint.breakText(currentLine, true, usableWidth, null)
                val part = currentLine.substring(0, charsThatFit)
                currentLine = currentLine.substring(charsThatFit)

                // If page is full → new page
                if (y + lineHeight > pageHeight - margin) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = margin
                }

                canvas.drawText(part, margin, y, paint)
                y += lineHeight
            }
        }

        pdfDocument.finishPage(page)

        val outputStream = ByteArrayOutputStream()
        pdfDocument.writeTo(outputStream)
        pdfDocument.close()

        return outputStream.toByteArray()
    }



    /** ---------------------- WORD ---------------------- **/
    private fun convertWordToPdfBytes(uri: Uri, fileName: String): ByteArray {
        val text = if (fileName.endsWith(".doc", true)) {
            val doc = HWPFDocument(contentResolver.openInputStream(uri))
            val range = doc.range
            (0 until range.numParagraphs()).joinToString("\n") {
                range.getParagraph(it).text().trim()
            }
        } else {
            val docx = XWPFDocument(contentResolver.openInputStream(uri))
            docx.paragraphs.joinToString("\n") { it.text }
        }
        return convertTextToPdfBytes(text) // reuse your existing PdfDocument code
    }

    /** ---------------------- EXCEL ---------------------- **/
    private fun convertExcelToPdfBytes(excelUri: Uri, fileName: String): ByteArray {
        val inputStream = contentResolver.openInputStream(excelUri)
            ?: throw Exception("Failed to open Excel file")
        val workbook = if (fileName.endsWith(".xls", true)) {
            HSSFWorkbook(inputStream)
        } else {
            XSSFWorkbook(inputStream)
        }

        // Use PdfDocument instead of PDDocument (Android compatible)
        val pdfDocument = PdfDocument()
        val paint = Paint().apply { textSize = 12f }

        var page = pdfDocument.startPage(PdfDocument.PageInfo.Builder(595, 842, 1).create())
        var y = 50f

        for (sheetIndex in 0 until workbook.numberOfSheets) {
            val sheet = workbook.getSheetAt(sheetIndex)
            for (row in sheet) {
                val rowText = row.joinToString(" | ") { cell ->
                    try {
                        cell.toString()
                    } catch (e: Exception) {
                        ""
                    }
                }

                if (y > 800) { // new page
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(
                        PdfDocument.PageInfo.Builder(
                            595,
                            842,
                            pdfDocument.pages.size + 1
                        ).create()
                    )
                    y = 50f
                }

                page.canvas.drawText(rowText, 50f, y, paint)
                y += 20f
            }
        }
        pdfDocument.finishPage(page)

        val bos = ByteArrayOutputStream()
        pdfDocument.writeTo(bos)
        pdfDocument.close()
        workbook.close()
        return bos.toByteArray()
    }


    /** ---------------------- PPT ---------------------- **/
    private fun convertPptToPdfBytes(pptUri: Uri, fileName: String): ByteArray {
        contentResolver.openInputStream(pptUri)?.use { inputStream ->
            val pdfDoc = PdfDocument()

            if (fileName.endsWith(".pptx", true)) {
                val ppt = XMLSlideShow(inputStream)
                for (slide in ppt.slides) {
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val page = pdfDoc.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = Paint().apply { textSize = 12f }
                    var y = 50f
                    for (shape in slide.shapes) {
                        if (shape is XSLFTextShape) {
                            val text = shape.text ?: ""
                            for (line in text.split("\n")) {
                                canvas.drawText(line, 50f, y, paint)
                                y += paint.descent() - paint.ascent()
                            }
                        }
                    }
                    pdfDoc.finishPage(page)
                }
            } else if (fileName.endsWith(".ppt", true)) {
                val ppt = HSLFSlideShow(inputStream)
                for (slide in ppt.slides) {
                    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
                    val page = pdfDoc.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = Paint().apply { textSize = 12f }
                    var y = 50f
                    for (shape in slide.shapes) {
                        if (shape is HSLFTextShape) {
                            val text = shape.text ?: ""
                            for (line in text.split("\n")) {
                                canvas.drawText(line, 50f, y, paint)
                                y += paint.descent() - paint.ascent()
                            }
                        }
                    }
                    pdfDoc.finishPage(page)
                }
            } else {
                throw Exception("Unsupported PPT format")
            }

            val bos = ByteArrayOutputStream()
            pdfDoc.writeTo(bos)
            pdfDoc.close()
            return bos.toByteArray()
        } ?: throw Exception("Failed to open PPT file")
    }

    private fun convertPdfToText(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri) ?: return ""
        val reader = PdfReader(inputStream)
        val pdfDoc = com.itextpdf.kernel.pdf.PdfDocument(reader)

        val strategy = SimpleTextExtractionStrategy()
        val sb = StringBuilder()

        for (i in 1..pdfDoc.numberOfPages) {
            val page = pdfDoc.getPage(i)
            val text = PdfTextExtractor.getTextFromPage(page, strategy)
            sb.append(text).append("\n")
        }

        pdfDoc.close()
        return sb.toString()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun renderPdfPageToBitmap(uri: Uri, pageIndex: Int): Bitmap? {
        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return null
        val renderer = PdfRenderer(fd)
        if (pageIndex >= renderer.pageCount) {
            renderer.close()
            return null
        }

        val page = renderer.openPage(pageIndex)
        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
        page.close()
        renderer.close()
        return bitmap
    }

    private fun runOcrWithMlKitSync(bitmap: Bitmap): String {
        return try {
            val image = InputImage.fromBitmap(bitmap, 0)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val result = Tasks.await(recognizer.process(image))
            result.text
        } catch (e: Exception) {
            ""
        }
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun convertPdfWithOcrToWord(uri: Uri): ByteArray {
        val doc = XWPFDocument()

        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return ByteArray(0)
        val renderer = PdfRenderer(fd)

        for (i in 0 until renderer.pageCount) {
            val bitmap = renderPdfPageToBitmap(uri, i) ?: continue
            val text = runOcrWithMlKitSync(bitmap)

            if (text.isNotBlank()) {
                // Add recognized text
                val para = doc.createParagraph()
                val run = para.createRun()
                run.setText(text)
            } else {
                // No text → insert image of page
                val para = doc.createParagraph()
                val run = para.createRun()

                // Convert Bitmap → ByteArray (PNG format)
                val stream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
                val imageBytes = stream.toByteArray()

                val pictureIdx = doc.addPictureData(imageBytes, XWPFDocument.PICTURE_TYPE_PNG)
                val helper = doc.createParagraph().createRun()
                helper.addPicture(
                    ByteArrayInputStream(imageBytes),
                    XWPFDocument.PICTURE_TYPE_PNG,
                    "page_${i + 1}.png",
                    Units.toEMU(bitmap.width.toDouble() / 2),  // scale width
                    Units.toEMU(bitmap.height.toDouble() / 2) // scale height
                )
            }
        }

        renderer.close()

        val bos = ByteArrayOutputStream()
        doc.write(bos)
        doc.close()
        return bos.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun convertPdfToExcelBytes(uri: Uri): ByteArray {
        val workbook = XSSFWorkbook()
        val sheet = workbook.createSheet("PDF Data")

        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return ByteArray(0)
        val renderer = PdfRenderer(fd)

        val helper = workbook.creationHelper
        val drawing = sheet.createDrawingPatriarch()

        for (i in 0 until renderer.pageCount) {
            val bitmap = renderPdfPageToBitmap(uri, i) ?: continue

            // OCR text
            val text = runOcrWithMlKitSync(bitmap)

            // Add text in column A
            val row = sheet.createRow(i)
            val cell = row.createCell(0)
            cell.setCellValue(if (text.isNotBlank()) text else "[Image Page]")

            // Convert Bitmap → ByteArray
            val imgStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imgStream)
            val pictureIdx = workbook.addPicture(imgStream.toByteArray(), Workbook.PICTURE_TYPE_PNG)

            // ✅ Use CreationHelper to create a generic anchor
            val anchor = helper.createClientAnchor()
            anchor.setCol1(1)
            anchor.setRow1(i)
            anchor.setCol2(2)
            anchor.setRow2(i + 1)

            // Add picture
            drawing.createPicture(anchor, pictureIdx)
        }

        renderer.close()

        val bos = ByteArrayOutputStream()
        workbook.write(bos)
        workbook.close()
        return bos.toByteArray()
    }


    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun convertPdfToPptBytes(uri: Uri): ByteArray {
        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return ByteArray(0)
        val renderer = PdfRenderer(fd)

        val bos = ByteArrayOutputStream()
        val zos = ZipOutputStream(bos)

        // [Content_Types].xml
        zos.putNextEntry(ZipEntry("[Content_Types].xml"))
        zos.write(
            """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Types xmlns="http://schemas.openxmlformats.org/package/2006/content-types">
          <Default Extension="rels" ContentType="application/vnd.openxmlformats-package.relationships+xml"/>
          <Default Extension="xml" ContentType="application/xml"/>
          <Default Extension="png" ContentType="image/png"/>
          <Override PartName="/ppt/presentation.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.presentation.main+xml"/>
          <Override PartName="/ppt/slides/slide1.xml" ContentType="application/vnd.openxmlformats-officedocument.presentationml.slide+xml"/>
        </Types>
    """.trimIndent().toByteArray()
        )
        zos.closeEntry()

        // Root relationships
        zos.putNextEntry(ZipEntry("_rels/.rels"))
        zos.write(
            """
        <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
        <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
          <Relationship Id="rId1"
            Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument"
            Target="ppt/presentation.xml"/>
        </Relationships>
    """.trimIndent().toByteArray()
        )
        zos.closeEntry()

        val slideEntries = mutableListOf<String>()

        for (i in 0 until renderer.pageCount) {
            val bitmap = renderPdfPageToBitmap(uri, i) ?: continue

            // Save image
            val imgName = "ppt/media/image${i + 1}.png"
            zos.putNextEntry(ZipEntry(imgName))
            val imgStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imgStream)
            zos.write(imgStream.toByteArray())
            zos.closeEntry()

            // Compute size in EMU
            val cx = bitmap.width * 9525L  // 1 pixel = 9525 EMUs
            val cy = bitmap.height * 9525L

            // Slide XML
            val slideXml = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <p:sld xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:a="http://schemas.openxmlformats.org/drawingml/2006/main"
                   xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">
              <p:cSld>
                <p:spTree>
                  <p:pic>
                    <p:nvPicPr>
                      <p:cNvPr id="1" name="Picture ${i + 1}"/>
                      <p:cNvPicPr/>
                      <p:nvPr/>
                    </p:nvPicPr>
                    <p:blipFill>
                      <a:blip r:embed="rId1"/>
                      <a:stretch><a:fillRect/></a:stretch>
                    </p:blipFill>
                    <p:spPr>
                      <a:xfrm>
                        <a:off x="0" y="0"/>
                        <a:ext cx="$cx" cy="$cy"/>
                      </a:xfrm>
                      <a:prstGeom prst="rect"><a:avLst/></a:prstGeom>
                    </p:spPr>
                  </p:pic>
                </p:spTree>
              </p:cSld>
            </p:sld>
        """.trimIndent()

            val slidePath = "ppt/slides/slide${i + 1}.xml"
            zos.putNextEntry(ZipEntry(slidePath))
            zos.write(slideXml.toByteArray())
            zos.closeEntry()
            slideEntries.add(slidePath)

            // Slide relationships (link to image)
            val relsPath = "ppt/slides/_rels/slide${i + 1}.xml.rels"
            zos.putNextEntry(ZipEntry(relsPath))
            zos.write(
                """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">
              <Relationship Id="rId1"
                Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/image"
                Target="../media/image${i + 1}.png"/>
            </Relationships>
        """.trimIndent().toByteArray()
            )
            zos.closeEntry()
        }

        // Presentation.xml linking slides
        val presXml = buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append(
                """<p:presentation xmlns:p="http://schemas.openxmlformats.org/presentationml/2006/main"
                   xmlns:r="http://schemas.openxmlformats.org/officeDocument/2006/relationships">"""
            )
            append("<p:sldIdLst>")
            slideEntries.forEachIndexed { idx, _ ->
                append("""<p:sldId id="${256 + idx}" r:id="rId${idx + 1}"/>""")
            }
            append("</p:sldIdLst>")
            append("</p:presentation>")
        }

        zos.putNextEntry(ZipEntry("ppt/presentation.xml"))
        zos.write(presXml.toByteArray())
        zos.closeEntry()

        // Presentation relationships
        zos.putNextEntry(ZipEntry("ppt/_rels/presentation.xml.rels"))
        zos.write(buildString {
            append("""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>""")
            append("""<Relationships xmlns="http://schemas.openxmlformats.org/package/2006/relationships">""")
            slideEntries.forEachIndexed { idx, path ->
                append(
                    """<Relationship Id="rId${idx + 1}" 
               Type="http://schemas.openxmlformats.org/officeDocument/2006/relationships/slide"
               Target="slides/slide${idx + 1}.xml"/>"""
                )
            }
            append("</Relationships>")
        }.toByteArray())
        zos.closeEntry()

        renderer.close()
        zos.close()
        return bos.toByteArray()
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private fun convertPdfToHtmlBytes(uri: Uri): ByteArray {
        val sb = StringBuilder()
        sb.append("<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Converted PDF</title></head><body>")

        val fd = contentResolver.openFileDescriptor(uri, "r") ?: return ByteArray(0)
        val renderer = PdfRenderer(fd)

        for (i in 0 until renderer.pageCount) {
            val bitmap = renderPdfPageToBitmap(uri, i) ?: continue
            val text = runOcrWithMlKitSync(bitmap)

            // Add text
            if (text.isNotBlank()) {
                sb.append("<p>").append(text).append("</p>")
            }

            // Add image
            val imgStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, imgStream)
            val base64Img = Base64.encodeToString(imgStream.toByteArray(), Base64.DEFAULT)
            sb.append("<img src='data:image/png;base64,").append(base64Img)
                .append("' style='max-width:100%;'><br>")
        }

        sb.append("</body></html>")
        renderer.close()

        return sb.toString().toByteArray(Charsets.UTF_8)
    }


    private fun saveFileToDocuments(fileName: String, subFolder: String, data: ByteArray): String {
        val appName = MyApp.getInstance().getString(R.string.app_name)
        val documentsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appDir = File(documentsDir, "$appName/$subFolder")

        if (!appDir.exists()) appDir.mkdirs()

        val file = File(appDir, fileName)
        FileOutputStream(file).use { it.write(data) }

        return file.absolutePath
    }
}