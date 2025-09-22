package com.app.figpdfconvertor.figpdf.activity

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.preference.PreferenceManager
import android.provider.MediaStore
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.BaseActivity
import com.app.figpdfconvertor.figpdf.adapter.EditedImagesAdapter
import com.app.figpdfconvertor.figpdf.databinding.ActivityConvertPdfactivityBinding
import com.app.figpdfconvertor.figpdf.dialogs.ChooseCardDialogFragment
import com.app.figpdfconvertor.figpdf.dialogs.DialogPDFComponent
import com.app.figpdfconvertor.figpdf.dialogs.DialogSortBy
import com.app.figpdfconvertor.figpdf.utils.Constants.DEFAULT_IMAGE_SCALE_TYPE_TEXT
import com.app.figpdfconvertor.figpdf.utils.Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO
import com.app.figpdfconvertor.figpdf.utils.Constants.MASTER_PWD_STRING
import com.app.figpdfconvertor.figpdf.utils.CreatePdf
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions
import com.app.figpdfconvertor.figpdf.utils.ImageUtils
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import com.app.figpdfconvertor.figpdf.utils.OnPDFCreatedInterface
import com.app.figpdfconvertor.figpdf.utils.PDFUtils
import com.app.figpdfconvertor.figpdf.utils.PageSizeUtils
import com.google.android.material.textfield.TextInputEditText
import io.appmetrica.analytics.AppMetrica
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ConvertPDFActivity : BaseActivity() {
    private lateinit var imagesList: MutableList<String>
    private var mPath: String? = null
    private var mSharedPreferences: SharedPreferences? = null


    private lateinit var binding: ActivityConvertPdfactivityBinding
    private lateinit var adapter: EditedImagesAdapter
    private var images: MutableList<String> = mutableListOf()
    private var mPDFUtils: PDFUtils? = null
    private var mPdfOptions: ImageToPDFOptions? = null
    private var pdfTitle: String? = null

    private val ADD_IMAGE = "ADD_IMAGE"

    private var sortField: SortField = SortField.NAME
    private var sortOrder: SortOrder = SortOrder.ASCENDING

    enum class SortField { NAME, CREATED, MODIFIED }
    enum class SortOrder { ASCENDING, DESCENDING }

    fun onSelectionStarted() {
        binding.rlmaintoolbar.visibility = View.GONE
        binding.rlSelectToolbar.visibility = View.VISIBLE
    }

    // Image Selection launcher
    private val imageSelectionLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val newImages = result.data?.getStringArrayListExtra("SELECTED_IMAGES") ?: arrayListOf()
            if (newImages.isNotEmpty()) {
                val addImageIndex = images.indexOf(ADD_IMAGE)
                if (addImageIndex != -1) {
                    images.addAll(addImageIndex, newImages) // Insert before ADD_IMAGE
                } else {
                    images.addAll(newImages)
                }
                adapter.notifyDataSetChanged()
            }
        }
    }

    internal fun previewPdf() {
        if (images.isEmpty() || images.all { it == ADD_IMAGE }) {
            Toast.makeText(this, "No images to preview", Toast.LENGTH_SHORT).show()
            return
        }

        // Filter valid images
        val validImages = images.filter { it != ADD_IMAGE }

        // Prepare temp PDF in cache
        val tempPdf = File(cacheDir, "preview.pdf")
        tempPdf.delete() // remove old if exists

        // Set up temporary PDF options
        val tempPdfOptions = ImageToPDFOptions().apply {
            setImagesUri(ArrayList(validImages))
            outFileName = "preview.pdf"
            pageSize = PageSizeUtils.mPageSize
            imageScaleType = ImageUtils.getInstance().mImageScaleType
            pdfOrientation = mPdfOptions?.pdfOrientation ?: ImageToPDFOptions.PdfOrientation.AUTO
            password = "" // no password for preview
            isPasswordProtected = false
            if (mPdfOptions?.isWhiteMargin == true) setMargins(20, 20, 20, 20)
        }

        // Create PDF in cache
        CreatePdf(tempPdfOptions, cacheDir.absolutePath, object : OnPDFCreatedInterface {
            override fun onPDFCreationStarted() {
                Toast.makeText(this@ConvertPDFActivity, "Preparing preview...", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPDFProgress(progress: Int) {}

            override fun onPDFCreated(success: Boolean, path: String?) {
                if (success && path != null) {
                    // Launch preview activity
                    val intent = Intent(this@ConvertPDFActivity, PDFViewerActivity::class.java)
                    intent.putExtra("pdf_path", path)
                    startActivity(intent)
                } else {
                    Toast.makeText(this@ConvertPDFActivity, "Failed to create preview", Toast.LENGTH_SHORT)
                        .show()
                }
            }
        }).execute()
    }


    // Image Edit launcher
    private val imageEditLauncher = registerForActivityResult(
        StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val editedImage = result.data?.getStringExtra("EDITED_IMAGE")
            val position = result.data?.getIntExtra("POSITION", -1) ?: -1
            if (editedImage != null && position != -1) {
                images[position] = editedImage  // update only this item
                adapter.notifyItemChanged(position)
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityConvertPdfactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // Initialize variables
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        images = intent.getStringArrayListExtra("editedUris")?.toMutableList() ?: mutableListOf()
        ImageUtils.getInstance().mImageScaleType = mSharedPreferences!!.getString(
            DEFAULT_IMAGE_SCALE_TYPE_TEXT,
            IMAGE_SCALE_TYPE_ASPECT_RATIO
        )
        // Add "special" last item for Add button
        images.add(ADD_IMAGE)
        mPDFUtils = PDFUtils(this)
        mPdfOptions = ImageToPDFOptions()

        /*  val outputDir = getPdfOutputFolder()
          val file = File(outputDir, "temp.pdf")
          mPath = file.absolutePath*/

        val sdf = SimpleDateFormat("yyyyMMdd HHmmss", Locale.getDefault())
        val currentDateTime = sdf.format(Date())

        binding.txtTitle.text = "Image To PDF $currentDateTime"

        binding.imgEdit.setOnClickListener {
            showRenameDialog(this)
        }
        setupBackPressed()
        // Create initial empty pdf if not exists
        /*  if (!file.exists()) {
              mPDFUtils!!.createPdfFromImages(
                  file.absolutePath,
                  ArrayList(images.filter { it != "ADD_IMAGE" })
              )
          }*/


        // 👉 Create initial empty pdf if not exists
        /* if (!file.exists()) {
             mPDFUtils!!.createPdfFromImages(
                 file.absolutePath,
                 ArrayList(images.filter { it != "ADD_IMAGE" })
             )
         }*/

        binding.imgAdd.setOnClickListener {
            val intent = Intent(this, ImageSelection::class.java)

            // Pass current images (excluding ADD_IMAGE)
            intent.putStringArrayListExtra(
                "existingImages",
                ArrayList(images.filter { it != ADD_IMAGE })
            )

            imageSelectionLauncher.launch(intent)
        }
        val editScreenLauncher = registerForActivityResult(StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val updatedList = result.data!!.getStringArrayListExtra("IMAGE_LIST")
                if (updatedList != null) {
                    images.clear()
                    images.addAll(updatedList)
                    images.add("ADD_IMAGE") // keep add button
                    adapter.notifyDataSetChanged()
                }
            }
        }

        /*if (images[pos] == ADD_IMAGE) {
            // Handle add image click
            val intent = Intent(this, ImageSelection::class.java)
            imageSelectionLauncher.launch(intent)
            Toast.makeText(this, "Add Image clicked", Toast.LENGTH_SHORT).show()
        } else {
            val intent = Intent(this, EditScreen::class.java)
            intent.putStringArrayListExtra(
                "IMAGE_LIST",
                ArrayList(images.filter { it != ADD_IMAGE })
            )
            intent.putExtra("POSITION", pos)
            editScreenLauncher.launch(intent)
        }*/
        adapter = EditedImagesAdapter(
            this,
            images,
            onItemClick = { pos ->
                if (images[pos] == ADD_IMAGE) {
                    // Open ImageSelectionActivity
                    val intent = Intent(this, ImageSelection::class.java)
                    intent.putStringArrayListExtra(
                        "existingImages",
                        ArrayList(images.filter { it != ADD_IMAGE })
                    )
                    imageSelectionLauncher.launch(intent)
                } else {
                    // Open EditScreen for editing
                    /*val intent = Intent(this, EditScreen::class.java)
                    intent.putExtra("IMAGE_PATH", images[pos])
                    intent.putExtra("POSITION", pos)
                    imageEditLauncher.launch(intent)*/
                    val intent = Intent(this, EditScreen::class.java)
                    intent.putStringArrayListExtra(
                        "IMAGE_LIST",
                        ArrayList(images.filter { it != ADD_IMAGE }) // exclude "ADD_IMAGE"
                    )
                    intent.putExtra("POSITION", pos)
                    editScreenLauncher.launch(intent)
                }
            },
            onSelectionChanged = { count ->
                binding.txtTitleSelect.text = "$count Selected"
                binding.imgDelete.visibility = if (count > 0) View.VISIBLE else View.GONE
            }
        )

        binding.recyclerView.adapter = adapter


        imagesList =
            intent.getStringArrayListExtra("editedUris")?.toMutableList() ?: mutableListOf()
        binding.imgDrag.setOnClickListener {
            val intent = Intent(this, DragItemActivity::class.java)
            intent.putStringArrayListExtra(
                "editedUris",
                imagesList as java.util.ArrayList<String?>?
            )
            startActivity(intent)
        }

        /*  adapter = EditedImagesAdapter(this,images) { position ->
              if (images[position] == "ADD_IMAGE") {
                  // Handle add image click
                  Toast.makeText(this, "Add Image clicked", Toast.LENGTH_SHORT).show()
                  // open gallery/camera here
              }
          }*/

        binding.imgMenu.setOnClickListener {
            val chooseCardDialog = ChooseCardDialogFragment(
                binding.txtTitle.text.toString(),
                object : ChooseCardDialogFragment.CardDialogListener {
                    override fun setPwClick() {
                        showSetPwDialog(this@ConvertPDFActivity)
                    }

                    override fun saveToGallery() {
                        saveImagesToGallery(images.filter { it != ADD_IMAGE })
                    }

                    override fun select() {
                        adapter.startSelectionMode()

                        /*if (adapter.isAllSelected()) {
                            // Already all selected → just deselect all but keep selection mode ON
                            adapter.deselectAllKeepMode()
                        } else {
                            // Not all selected → select all
                            adapter.selectionMode = true  // ensure mode is ON
                            adapter.selectAll()
                        }*/
                        binding.txtTitleSelect.text = "${adapter.getSelectedCount()} Selected"
                    }

                    override fun sortBy() {
                        val sortbyDialog = DialogSortBy(
                            binding.txtTitle.text.toString(),
                            object : DialogSortBy.CardDialogListener {

                                override fun setName() {
                                    sortField = SortField.NAME
                                    applySort()
                                }

                                override fun setCreatedDate() {
                                    sortField = SortField.CREATED
                                    applySort()
                                }

                                override fun setModifiedDate() {
                                    sortField = SortField.MODIFIED
                                    applySort()
                                }

                                override fun setAscending() {
                                    sortOrder = SortOrder.ASCENDING
                                    applySort()
                                }

                                override fun setDescending() {
                                    sortOrder = SortOrder.DESCENDING
                                    applySort()
                                }
                            }
                        )
                        sortbyDialog.show(supportFragmentManager, "SortByDialog")

                    }
                }
            )
            chooseCardDialog.show(supportFragmentManager, "ChooseCardDialog")
        }
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
        binding.imgSelect.setOnClickListener {
            if (adapter.isAllSelected()) {
                // Already all selected → just deselect all but keep selection mode ON
                adapter.deselectAllKeepMode()
            } else {
                // Not all selected → select all
                adapter.selectionMode = true  // ensure mode is ON
                adapter.selectAll()
            }
            binding.txtTitleSelect.text = "${adapter.getSelectedCount()} Selected"
        }

        binding.imgBack.setOnClickListener {
            showExitDialog(this)
        }

        binding.imgBackSelect.setOnClickListener {
            adapter.clearSelection()
            binding.rlSelectToolbar.visibility = View.GONE
            binding.rlmaintoolbar.visibility = View.VISIBLE
        }

        binding.imgDelete.setOnClickListener {
            adapter.deleteSelected()
            binding.rlSelectToolbar.visibility = View.GONE
            binding.rlmaintoolbar.visibility = View.VISIBLE
        }

        binding.rlPdfConvert.setOnClickListener {
            val openPDDComponentDialog = DialogPDFComponent(
                binding.txtTitle.text.toString(),
                mPdfOptions!!,
                object : DialogPDFComponent.CardDialogListener {

                    override fun setPassword() {
                        if (!mPdfOptions!!.isPasswordProtected) {
                            showSetPwDialog(this@ConvertPDFActivity)
                        } else {
                            mPdfOptions!!.isPasswordProtected = false
                        }

                    }

                    override fun convert() {
                        val eventParams: MutableMap<String, Any?> = HashMap()
                        eventParams["action"] = "convert_pdf"
                        eventParams["file_name"] = "Converted_123.pdf"
                        eventParams["time"] = System.currentTimeMillis()

                        AppMetrica.reportEvent("PDF_Conversion_Started", eventParams)
                        convertImagesToPdf(ArrayList(images.filter { it != ADD_IMAGE }))

                    }
                }
            )
            openPDDComponentDialog.show(supportFragmentManager, "ChooseCardDialog")
        }

        /* val callback = object : ItemTouchHelper.SimpleCallback(
             ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
             0
         ) {
             override fun onMove(
                 recyclerView: RecyclerView,
                 viewHolder: RecyclerView.ViewHolder,
                 target: RecyclerView.ViewHolder
             ): Boolean {
                 adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                 return true
             }

             override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                 // Not needed
             }

             override fun isLongPressDragEnabled(): Boolean = true
         }
 */
        val callback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.START or ItemTouchHelper.END,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder,
            ): Boolean {
                adapter.moveItem(viewHolder.adapterPosition, target.adapterPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                // Not needed
            }

            override fun isLongPressDragEnabled(): Boolean = true

            // 👇 This makes the dragging item transparent
            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.5f   // 50% transparent
                }
            }

            // 👇 Restore after drop
            override fun clearView(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
            ) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f   // restore full opacity
            }
        }

        ItemTouchHelper(callback).attachToRecyclerView(binding.recyclerView)
    }

    private fun applySort() {
        // Exclude the "ADD_IMAGE" item
        val imagesToSort = images.filter { it != ADD_IMAGE }

        val sortedList = when (sortField) {
            SortField.NAME -> imagesToSort.sortedBy { File(it).name.lowercase(Locale.getDefault()) }
            SortField.CREATED -> imagesToSort.sortedBy {
                File(it).takeIf { f -> f.exists() }?.let { it.lastModified() } ?: 0L
            }

            SortField.MODIFIED -> imagesToSort.sortedBy {
                File(it).takeIf { f -> f.exists() }?.let { it.lastModified() } ?: 0L
            }
        }

        val finalList = if (sortOrder == SortOrder.ASCENDING) sortedList else sortedList.reversed()

        // Add the "ADD_IMAGE" button at the end
        images.clear()
        images.addAll(finalList)
        images.add(ADD_IMAGE)

        adapter.notifyDataSetChanged()
    }


    fun showRenameDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null)
        val etRename = dialogView.findViewById<TextInputEditText>(R.id.etRename)
        val imgRemove = dialogView.findViewById<ImageView>(R.id.imgRemove)
        val cancelBtn = dialogView.findViewById<RelativeLayout>(R.id.cancelButton)
        val okBtn = dialogView.findViewById<RelativeLayout>(R.id.okButton)

        // Set current PDF title in EditText
        etRename.setText(pdfTitle ?: binding.txtTitle.text.toString())

        imgRemove.setOnClickListener { etRename.text?.clear() }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        okBtn.setOnClickListener {
            val newName = etRename.text.toString().trim()
            if (newName.isNotEmpty()) {
                pdfTitle = newName               // ✅ store the PDF title
                binding.txtTitle.text = newName  // ✅ update UI
            }
            dialog.dismiss()
        }

        dialog.show()
    }


    private fun generatePdfName(): String {
        return pdfTitle?.takeIf { it.isNotEmpty() }?.let { "$it.pdf" } ?: run {
            val sdf = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            "ConvertedPdf_${sdf.format(Date())}.pdf"
        }
    }


    private fun saveImagesToGallery(imagePaths: List<String>) {
        val resolver = contentResolver

        for (path in imagePaths) {
            try {
                val inputStream: InputStream? = when {
                    path.startsWith("content://") -> {
                        resolver.openInputStream(Uri.parse(path))
                    }

                    path.startsWith("file://") -> {
                        resolver.openInputStream(Uri.parse(path))   // instead of File(path)
                    }

                    else -> {
                        val file = File(path)
                        if (file.exists()) FileInputStream(file) else null
                    }
                }


                if (inputStream != null) {
                    val fileName = "IMG_${System.currentTimeMillis()}.jpg"

                    val values = ContentValues().apply {
                        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                        put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/FigConvertor")
                        put(MediaStore.Images.Media.IS_PENDING, 1)
                    }

                    val collection =
                        MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
                    val uri = resolver.insert(collection, values)

                    if (uri != null) {
                        resolver.openOutputStream(uri)?.use { outStream ->
                            inputStream.use { it.copyTo(outStream) }
                        }

                        // Mark as complete
                        values.clear()
                        values.put(MediaStore.Images.Media.IS_PENDING, 0)
                        resolver.update(uri, values, null, null)
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Toast.makeText(this, "Images saved in Gallery → Pictures/FigConvertor", Toast.LENGTH_SHORT)
            .show()
    }

    fun showSetPwDialog(context: Context) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_password, null)

        val etRename = dialogView.findViewById<TextInputEditText>(R.id.etRename)
        val imgRemove = dialogView.findViewById<ImageView>(R.id.imgRemove)
        val okBtn = dialogView.findViewById<RelativeLayout>(R.id.okButton)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val isPasswordVisible = booleanArrayOf(false)

        imgRemove.setOnClickListener(View.OnClickListener { v: View? ->
            if (isPasswordVisible[0]) {
                // Hide password
                etRename.setInputType(
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                )
                imgRemove.setImageResource(R.drawable.ic_pw_eye) // eye icon
                isPasswordVisible[0] = false
            } else {
                // Show password
                etRename.setInputType(
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                )
                imgRemove.setImageResource(R.drawable.ic_pw_eye) // optional open eye icon
                isPasswordVisible[0] = true
            }
            // Move cursor to the end
            etRename.setSelection(etRename.getText()!!.length)
        })
        okBtn.setOnClickListener {
            val password = etRename.text.toString().trim()
            mPdfOptions?.password = password
            mPdfOptions?.isPasswordProtected = password.isNotEmpty()
            dialog.dismiss()
        }
        imgRemove.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    /*private fun convertImagesToPdf(imagePaths: ArrayList<String>, isGrayScale: Boolean = false) {
        if (imagePaths.isEmpty()) {
            Toast.makeText(this, "No images to convert", Toast.LENGTH_SHORT).show()
            return
        }

        // Normalize and filter only valid files
        val validImages = imagePaths.mapNotNull { path ->
            when {
                path.startsWith("content://") -> {
                    // Test if content URI is readable
                    try {
                        contentResolver.openInputStream(Uri.parse(path))?.close()
                        path
                    } catch (e: Exception) {
                        null
                    }
                }
                path.startsWith("file://") || path.startsWith("file:/") -> {
                    val file = File(Uri.parse(path).path ?: path)
                    if (file.exists()) file.absolutePath else null
                }
                else -> {
                    val file = File(path)
                    if (file.exists()) file.absolutePath else null
                }
            }
        }

        if (validImages.isEmpty()) {
            Toast.makeText(this, "No valid images found to convert", Toast.LENGTH_SHORT).show()
            return
        }

        // Optional: grayscale
        if (isGrayScale) saveImagesInGrayScale(ArrayList(validImages))

        // Prepare PDF options
        mPdfOptions?.apply {
            setImagesUri(ArrayList(validImages))
            outFileName = generatePdfName()
            pageSize = PageSizeUtils.mPageSize
            imageScaleType = ImageUtils.getInstance().mImageScaleType
            pageNumStyle = mPageNumStyle
            masterPwd = mSharedPreferences?.getString(MASTER_PWD_STRING, "FigConvertor")
            setPageColor(mPageColor)
            password = password
            isPasswordProtected = !password.isNullOrEmpty()
            if (mWhiteMargin) {
                setMargins(20, 20, 20, 20) // default white margin
            } else {
                setMargins(0, 0, 0, 0)
            }
        }

        // PDF output folder
        val outputDir = getPdfOutputFolder()

        CreatePdf(mPdfOptions!!, outputDir.absolutePath, object : OnPDFCreatedInterface {
            override fun onPDFCreationStarted() {
                Toast.makeText(this@ConvertPDFActivity, "PDF creation started", Toast.LENGTH_SHORT).show()
            }

            override fun onPDFCreated(success: Boolean, path: String?) {
                if (success && path != null) {
                    Toast.makeText(this@ConvertPDFActivity, "PDF saved at:\n$path", Toast.LENGTH_LONG).show()
                    mPath = path
                } else {
                    Toast.makeText(this@ConvertPDFActivity, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                }
            }
        }).execute()
    }*/

    private fun convertImagesToPdf(imagePaths: ArrayList<String>, isGrayScale: Boolean = false) {

        if (imagePaths.isEmpty()) {
            Toast.makeText(this, "No images to convert", Toast.LENGTH_SHORT).show()
            return
        }

        // Normalize and filter only valid files
        val validImages = imagePaths.mapNotNull { path ->
            when {
                path.startsWith("content://") -> try {
                    contentResolver.openInputStream(Uri.parse(path))?.close(); path
                } catch (e: Exception) {
                    null
                }

                path.startsWith("file://") || path.startsWith("file:/") -> File(
                    Uri.parse(path).path ?: path
                ).takeIf { it.exists() }?.absolutePath

                else -> File(path).takeIf { it.exists() }?.absolutePath
            }
        }

        if (validImages.isEmpty()) {
            Toast.makeText(this, "No valid images found to convert", Toast.LENGTH_SHORT).show()
            return
        }

        // Optional: grayscale
        if (isGrayScale) saveImagesInGrayScale(ArrayList(validImages))

        // Compression settings (adjust as needed)
        val compressionQuality = 80  // 0-100
        val compressionScale = 0.5f  // e.g., 0.5 = 50% of original size

        // Prepare temporary folder for scaled images
        val tempDir = File(cacheDir, "temp_images")
        if (!tempDir.exists()) tempDir.mkdirs()

        val tempFiles = ArrayList<String>()

        // Compress & scale images
        for ((index, imagePath) in validImages.withIndex()) {
            val bitmap = getBitmapFromPath(imagePath) ?: continue

            val scaledBitmap = Bitmap.createScaledBitmap(
                bitmap,
                (bitmap.width * compressionScale).toInt(),
                (bitmap.height * compressionScale).toInt(),
                true
            )

            val tempFile = File(tempDir, "img_$index.jpg")
            FileOutputStream(tempFile).use { out ->
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, compressionQuality, out)
            }

            tempFiles.add(tempFile.absolutePath)

            // Clean up bitmaps
            bitmap.recycle()
            scaledBitmap.recycle()
        }

        // Set ImageToPDFOptions
        mPdfOptions?.apply {
            setImagesUri(tempFiles) // ✅ use compressed images
            outFileName = if (!outFileName.isNullOrEmpty()) outFileName else generatePdfName()
            pageSize = PageSizeUtils.mPageSize
            imageScaleType = ImageUtils.getInstance().mImageScaleType
            pageNumStyle = mPageNumStyle
            masterPwd = mSharedPreferences?.getString(MASTER_PWD_STRING, "FigConvertor")
            setPageColor(mPageColor)
            password = password
            isPasswordProtected = !password.isNullOrEmpty()
            if (mWhiteMargin) setMargins(20, 20, 20, 20) else setMargins(0, 0, 0, 0)
        }

//        val outputDir = getPdfOutputFolder()
        val outputDir = getPdfOutputFolder()
        // Create PDF
        CreatePdf(mPdfOptions!!, outputDir.absolutePath, object : OnPDFCreatedInterface {
            override fun onPDFCreationStarted() {
                binding.llProcess.visibility = View.VISIBLE
                binding.llMain.visibility = View.GONE  // Optional: dim background
                binding.linearProgressIndicator.progress = 0
                Toast.makeText(this@ConvertPDFActivity, "PDF creation started", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onPDFProgress(progress: Int) {
                // Optional: Update progress
                binding.linearProgressIndicator.progress = progress
                binding.txtProgress.text = "$progress% completed"
            }

            override fun onPDFCreated(success: Boolean, path: String?) {
                /*if (success && path != null) {
                    Toast.makeText(this@ConvertPDFActivity, "PDF saved at:\n$path", Toast.LENGTH_LONG).show()
                    mPath = path
                } else {
                    Toast.makeText(this@ConvertPDFActivity, "Failed to create PDF", Toast.LENGTH_SHORT).show()
                }

                // ✅ Clean up temporary files
                tempFiles.forEach { tempPath ->
                    try { File(tempPath).delete() } catch (e: Exception) { e.printStackTrace() }
                }
                if (tempDir.exists() && tempDir.isDirectory && tempDir.listFiles()?.isEmpty() == true) {
                    tempDir.delete()
                }*/
                if (success && path != null) {
                    Toast.makeText(
                        this@ConvertPDFActivity,
                        "PDF saved at:\n$path",
                        Toast.LENGTH_LONG
                    ).show()
                    mPath = path

                    // Start SuccessfullyConvertActivity
                    val intent =
                        Intent(this@ConvertPDFActivity, SuccessfullyConvertActivity::class.java)
                    intent.putExtra("pdf_path", path)
                    intent.putExtra("pdf_name", File(path).name)
                    startActivity(intent)
                    finish()
                } else {
                    Toast.makeText(
                        this@ConvertPDFActivity,
                        "Failed to create PDF",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                // Clean up temporary files
                tempFiles.forEach { tempPath -> File(tempPath).delete() }
                val tempDir = File(cacheDir, "temp_images")
                if (tempDir.exists() && tempDir.isDirectory && tempDir.listFiles()
                        ?.isEmpty() == true
                ) tempDir.delete()
            }
        }).execute()
    }


    /** Helper: Convert image to Bitmap safely from any path type */
    fun getBitmapFromPath(path: String): Bitmap? {
        return try {
            when {
                path.startsWith("content://") -> contentResolver.openInputStream(Uri.parse(path))
                    .use { BitmapFactory.decodeStream(it) }

                path.startsWith("file://") -> {
                    val file = File(Uri.parse(path).path!!)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }

                else -> {
                    val file = File(path)
                    if (file.exists()) BitmapFactory.decodeFile(file.absolutePath) else null
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }


    /** Optional: Convert images to grayscale */
    private fun saveImagesInGrayScale(imagePaths: ArrayList<String>) {
        for (i in imagePaths.indices) {
            val bitmap = getBitmapFromPath(imagePaths[i]) ?: continue
            val grayBitmap =
                Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
            val canvas = android.graphics.Canvas(grayBitmap)
            val paint = android.graphics.Paint()
            val colorMatrix = android.graphics.ColorMatrix()
            colorMatrix.setSaturation(0f)
            val filter = android.graphics.ColorMatrixColorFilter(colorMatrix)
            paint.colorFilter = filter
            canvas.drawBitmap(bitmap, 0f, 0f, paint)

            // Save back to same path
            val file =
                if (imagePaths[i].startsWith("file://")) File(Uri.parse(imagePaths[i]).path!!) else File(
                    imagePaths[i]
                )
            try {
                FileOutputStream(file).use { out ->
                    grayBitmap.compress(Bitmap.CompressFormat.JPEG, 100, out)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun getPdfOutputFolder(): File {
        val docs = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val appFolder = File(docs, getString(R.string.app_name) + "/ImageToPDF")
        if (!appFolder.exists()) appFolder.mkdirs()
        return appFolder
    }

    private fun setupBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            public override fun handleOnBackPressed() {
                showExitDialog(this@ConvertPDFActivity)
            }
        }
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private var exitDialog: AlertDialog? = null
    fun showExitDialog(context: Context) {
        if (exitDialog != null && exitDialog!!.isShowing()) return  // avoid multiple dialogs


        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_exit, null)

        val cancelBtn = dialogView.findViewById<RelativeLayout?>(R.id.cancelButton)
        val okBtn = dialogView.findViewById<RelativeLayout?>(R.id.okButton)

        exitDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        if (exitDialog!!.getWindow() != null) {
            exitDialog!!.getWindow()!!.setBackgroundDrawableResource(android.R.color.transparent)
        }

        cancelBtn.setOnClickListener(View.OnClickListener { v: View? -> exitDialog!!.dismiss() })

        okBtn.setOnClickListener(View.OnClickListener { v: View? ->
            finish()
            exitDialog!!.dismiss()
        })

        exitDialog!!.show()
    }
}