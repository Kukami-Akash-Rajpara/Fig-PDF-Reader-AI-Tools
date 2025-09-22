package com.app.figpdfconvertor.figpdf.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.scale
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.adapter.FiltersAdapter
import com.app.figpdfconvertor.figpdf.databinding.ActivityImageEditBinding
import com.app.figpdfconvertor.figpdf.adapter.ViewPagerAdapter
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import com.app.figpdfconvertor.figpdf.utils.ImageFilterUtils
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class ImageEditActivity : BaseActivity() {

    private lateinit var binding: ActivityImageEditBinding
    private lateinit var imageUris: ArrayList<String>
    private lateinit var pagerAdapter: ViewPagerAdapter
    private lateinit var filterAdapter: FiltersAdapter

    private val originalBitmaps = mutableMapOf<Int, Bitmap>()
    private val previewBitmaps = mutableMapOf<Int, Bitmap>()
    private val editedBitmaps = mutableMapOf<Int, Bitmap>()
    private lateinit var editImageLauncher: ActivityResultLauncher<Intent>

    private val fileNames = mutableMapOf<Int, String>() // Per-image file name

    private var tempBitmap: Bitmap? = null
    private var currentMode = "NONE"

    private var contrastValue = 50
    private var brightnessValue = 50
    private var detailsValue = 100

    private var applyJob: Job? = null

    private val currentIndex: Int
        get() = binding.viewPager.currentItem

    companion object {
        private const val REQ_CROP = 9001
        private const val MAX_PREVIEW = 1024
        private const val MAX_ORIGINAL = 3000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityImageEditBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        imageUris = intent.getStringArrayListExtra("imageUris") ?: arrayListOf()
        if (imageUris.isEmpty()) {
            imageUris.add(Uri.parse("android.resource://$packageName/${R.drawable.img_demo}").toString())
        }

        setupViewPager()
        bindClicks()
        setupBackPressed()
        lifecycleScope.launch(Dispatchers.IO) {
            imageUris.forEachIndexed { i, u ->
                val uri = Uri.parse(u)
                val orig = decodeBitmapFromUri(uri, MAX_ORIGINAL) ?: return@forEachIndexed
                val editable = orig.copy(orig.config ?: Bitmap.Config.ARGB_8888, true)
                val preview = getPreviewBitmap(editable, MAX_PREVIEW)

                originalBitmaps[i] = editable
                editedBitmaps[i] = editable.copy(editable.config ?: Bitmap.Config.ARGB_8888, true)
                previewBitmaps[i] = preview
                fileNames[i] = getFileNameWithoutExtension(uri, this@ImageEditActivity)
            }

            withContext(Dispatchers.Main) {
                initFiltersRecycler()
                updateFileNameAndFilters(currentIndex)
                editedBitmaps[currentIndex]?.let { pagerAdapter.updateBitmapAt(currentIndex, it) }
            }
        }

        editImageLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK && result.data != null) {
                val pos = result.data?.getIntExtra("POSITION", -1) ?: -1
                val editedPath = result.data?.getStringExtra("EDITED_IMAGE_PATH")

                if (editedPath != null && pos != -1) {
                    val bmp = decodeBitmapFromUri(Uri.parse(editedPath), MAX_ORIGINAL) ?: return@registerForActivityResult
                    editedBitmaps[pos] = bmp
                    previewBitmaps[pos] = getPreviewBitmap(bmp, MAX_PREVIEW)
                    pagerAdapter.updateBitmapAt(pos, bmp)
                    if (::filterAdapter.isInitialized && pos == currentIndex) {
                        filterAdapter.updateBaseBitmap(bmp)
                    }
                }
            }
        }
    }

    private fun setupViewPager() {
        pagerAdapter = ViewPagerAdapter(this, imageUris)
        binding.viewPager.apply {
            adapter = pagerAdapter
            offscreenPageLimit = 1
            registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
                override fun onPageSelected(position: Int) {
                    super.onPageSelected(position)
                    binding.viewPager.post {
                        updateFileNameAndFilters(position)
                        editedBitmaps[position]?.let { pagerAdapter.updateBitmapAt(position, it) }
                    }
                }
            })
        }
    }

    private fun bindClicks() {
        binding.imgBack.setOnClickListener { showExitDialog(this) }

        binding.imgDelete.setOnClickListener {
            val pos = currentIndex
            if (imageUris.isEmpty()) return@setOnClickListener

            pagerAdapter.removeAt(pos)
            imageUris.removeAt(pos)
            originalBitmaps.remove(pos)
            previewBitmaps.remove(pos)
            editedBitmaps.remove(pos)
            fileNames.remove(pos)
            reindexMapsAfterRemoval(pos)

            if (imageUris.isEmpty()) finish()
            else {
                val fixPos = currentIndex.coerceAtMost(imageUris.lastIndex)
                updateFileNameAndFilters(fixPos)
            }
        }

        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.textPercentage.text = progress.toString()
                val prev = editedBitmaps[currentIndex] ?: return
                when (currentMode) {
                    "contrast" -> updatePreview(ImageFilterUtils.applyContrast(prev, progress / 50f))
                    "brightness" -> updatePreview(ImageFilterUtils.applyBrightness(prev, (progress - 50) * 2f))
                    "details" -> updatePreview(ImageFilterUtils.applySharpness(prev, progress / 50f))
                }
                when (currentMode) {
                    "contrast" -> contrastValue = progress
                    "brightness" -> brightnessValue = progress
                    "details" -> detailsValue = progress
                }
            }

            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })

        binding.llDone.setOnClickListener { saveTempBitmapAndGoNext() }
        binding.imgDone.setOnClickListener { applyChanges() }
        binding.imgClose.setOnClickListener { discardChanges() }

        binding.llContrast.setOnClickListener { switchMode("contrast", 100, contrastValue) }
        binding.llBrightness.setOnClickListener { switchMode("brightness", 100, brightnessValue) }
        binding.llDetails.setOnClickListener { switchMode("details", 200, detailsValue) }
        binding.llAdjust.setOnClickListener { binding.llAdjustSetting.visibility = View.VISIBLE }

        binding.llLeft.setOnClickListener { rotateCurrent(-90f) }
        binding.llRight.setOnClickListener { rotateCurrent(90f) }
        binding.llCrop.setOnClickListener { openCustomCrop() }
        binding.imgEdit.setOnClickListener { showRenameDialog(this) }
    }

    private fun initFiltersRecycler() {
        val names = listOf("Original","Docs","Image","Super","Enhance","Enhance2","B&W","B&W2","Gray","Invert")
        val preview = previewBitmaps[currentIndex] ?: getPreviewBitmap(editedBitmaps[currentIndex]!!, MAX_PREVIEW)

        filterAdapter = FiltersAdapter(this, preview, names) { filterName ->
            val basePreview = previewBitmaps[currentIndex] ?: return@FiltersAdapter
            lifecycleScope.launch(Dispatchers.Default) {
                val filteredPreview = ImageFilterUtils.applyFilter(basePreview, filterName)
                withContext(Dispatchers.Main) {
                    // just show preview
                    tempBitmap = filteredPreview
                    updatePageImage(filteredPreview)
                }
            }
        }


        binding.filtersRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@ImageEditActivity, LinearLayoutManager.HORIZONTAL, false)
            adapter = filterAdapter
        }
    }


    private fun updateFileNameAndFilters(position: Int) {
        if (position !in imageUris.indices) return
        val name = fileNames[position] ?: getFileNameWithoutExtension(Uri.parse(imageUris[position]), this)
        binding.txtImageName.text = name

        val bmp = editedBitmaps[position] ?: originalBitmaps[position] ?: return
        if (::filterAdapter.isInitialized) filterAdapter.updateBaseBitmap(bmp)
    }

    private fun switchMode(mode: String, max: Int, value: Int) {
        currentMode = mode
        binding.seekBar.max = max
        binding.seekBar.progress = value
        binding.textPercentage.text = value.toString()
        binding.llAdjustSetting.visibility = View.VISIBLE
    }

    private fun updatePreview(bmp: Bitmap) {
        applyJob?.cancel()
        applyJob = lifecycleScope.launch(Dispatchers.Main) {
            tempBitmap?.recycle()
            tempBitmap = bmp
            updatePageImage(bmp)
        }
    }

    private fun applyChanges() {
        val bmp = tempBitmap ?: return
        editedBitmaps[currentIndex] = bmp.copy(Bitmap.Config.ARGB_8888, true)
        previewBitmaps[currentIndex] = getPreviewBitmap(bmp, MAX_PREVIEW)
        pagerAdapter.updateBitmapAt(currentIndex, editedBitmaps[currentIndex]!!)
        tempBitmap = null
        binding.llAdjustSetting.visibility = View.GONE
    }

    private fun discardChanges() {
        val orig = editedBitmaps[currentIndex] ?: return
        updatePageImage(orig)
        tempBitmap = null
        binding.llAdjustSetting.visibility = View.GONE
    }

    private fun updatePageImage(bmp: Bitmap) {
        pagerAdapter.updateBitmapAt(currentIndex, bmp)
    }

    private fun rotateCurrent(angle: Float) {
        val src = editedBitmaps[currentIndex] ?: return
        val rotated = rotateBitmap(src, angle)
        editedBitmaps[currentIndex] = rotated
        previewBitmaps[currentIndex] = getPreviewBitmap(rotated, MAX_PREVIEW)
        updatePageImage(rotated)
        if (::filterAdapter.isInitialized) filterAdapter.updateBaseBitmap(rotated)
    }

    private fun rotateBitmap(source: Bitmap, angle: Float): Bitmap {
        val m = Matrix().apply { postRotate(angle) }
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, m, true)
    }

    private fun saveTempBitmapAndGoNext() {
        // Apply temporary adjustments before saving
        tempBitmap?.let { bmp ->
            editedBitmaps[currentIndex] = bmp.copy(Bitmap.Config.ARGB_8888, true)
            previewBitmaps[currentIndex] = getPreviewBitmap(bmp, MAX_PREVIEW)
            updatePageImage(editedBitmaps[currentIndex]!!)
            tempBitmap = null
        }

        val editedUris = ArrayList<String>()
        editedBitmaps.toSortedMap().forEach { (i, bmp) ->
            // Save the edited bitmap to a file and get URI
            val originalName = getFileNameWithoutExtension(Uri.parse(imageUris[i]), this)
            val nameToUse = fileNames[i] ?: originalName
            val uri = getImageUriFromBitmap(this, bmp, nameToUse, originalName)
            editedUris.add(uri.toString())
        }

        // Pass the edited image URIs to ConvertPDFActivity
        val intent = Intent(this, ConvertPDFActivity::class.java).apply {
            putStringArrayListExtra("editedUris", editedUris)
        }
        startActivity(intent)
        finish()
    }

    private fun setupBackPressed() {
        val callback: OnBackPressedCallback = object : OnBackPressedCallback(true) {
            public override fun handleOnBackPressed() {
                showExitDialog(this@ImageEditActivity)
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
    private fun openCustomCrop() {
        val bmp = editedBitmaps[currentIndex] ?: return
        val srcUri = getImageUriFromBitmap(this, bmp)

        val intent = Intent(this@ImageEditActivity, EditImage::class.java)
        intent.putExtra("IMAGE_PATH", srcUri.toString())
        intent.putExtra("POSITION", currentIndex)
        intent.putExtra(EditScreen.Constants.EXTRA_SELECTED_TOOL, 0)
        intent.putExtra("SHOW_ONLY_DONE", true)
        editImageLauncher.launch(intent)
    }

    private fun reindexMapsAfterRemoval(removedIndex: Int) {
        fun <T> reindex(map: MutableMap<Int, T>) {
            val entries = map.toSortedMap().toList()
            map.clear()
            var i = 0
            entries.forEach { (oldIdx, v) ->
                if (oldIdx == removedIndex) return@forEach
                map[i++] = v
            }
        }
        reindex(originalBitmaps)
        reindex(previewBitmaps)
        reindex(editedBitmaps)

        val names = fileNames.toSortedMap().toList()
        fileNames.clear()
        var idx = 0
        names.forEach { (oldIdx, value) ->
            if (oldIdx != removedIndex) fileNames[idx++] = value
        }
    }

    private fun decodeBitmapFromUri(uri: Uri, maxDim: Int): Bitmap? {
        return try {
            val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, bounds) }
            val (w, h) = bounds.outWidth to bounds.outHeight
            if (w <= 0 || h <= 0) return null

            val sample = computeInSampleSize(w, h, maxDim, maxDim)
            val opts = BitmapFactory.Options().apply {
                inSampleSize = sample
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            contentResolver.openInputStream(uri).use { ins -> BitmapFactory.decodeStream(ins, null, opts) }
        } catch (_: Throwable) { null }
    }

    private fun computeInSampleSize(w: Int, h: Int, reqW: Int, reqH: Int): Int {
        var inSampleSize = 1
        if (h > reqH || w > reqW) {
            var halfH = h / 2
            var halfW = w / 2
            while ((halfH / inSampleSize) >= reqH && (halfW / inSampleSize) >= reqW) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun getImageUriFromBitmap(
        context: Context,
        bitmap: Bitmap,
        customName: String? = null,
        originalName: String? = null,
    ): Uri {
        val safeName = when {
            !customName.isNullOrBlank() -> customName
            !originalName.isNullOrBlank() -> originalName
            else -> "temp_${System.currentTimeMillis()}"
        }

        val file = File(context.cacheDir, "$safeName.jpg")
        file.outputStream().use { bitmap.compress(Bitmap.CompressFormat.JPEG, 100, it) }
        return Uri.fromFile(file)
    }

    private fun getPreviewBitmap(src: Bitmap, maxSize: Int): Bitmap {
        val scale = minOf(maxSize.toFloat() / src.width, maxSize.toFloat() / src.height, 1f)
        return src.scale((src.width * scale).toInt(), (src.height * scale).toInt())
    }

    fun getFileNameWithoutExtension(uri: Uri, context: Context): String {
        var name = ""
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx != -1) name = c.getString(idx)
                }
            }
        } else if (uri.scheme == "file") {
            name = uri.lastPathSegment ?: ""
        }
        return if (name.contains(".")) name.substringBeforeLast(".") else name
    }

    fun showRenameDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_rename, null)
        val etRename = dialogView.findViewById<TextInputEditText>(R.id.etRename)
        val imgRemove = dialogView.findViewById<ImageView>(R.id.imgRemove)
        val cancelBtn = dialogView.findViewById<RelativeLayout>(R.id.cancelButton)
        val okBtn = dialogView.findViewById<RelativeLayout>(R.id.okButton)

        etRename.setText(fileNames[currentIndex])

        imgRemove.setOnClickListener { etRename.text?.clear() }

        val dialog = AlertDialog.Builder(context).setView(dialogView).setCancelable(true).create()
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        cancelBtn.setOnClickListener { dialog.dismiss() }
        okBtn.setOnClickListener {
            val newName = etRename.text.toString().trim()
            if (newName.isNotEmpty()) {
                fileNames[currentIndex] = newName
                binding.txtImageName.text = newName

                val bmp = editedBitmaps[currentIndex] ?: return@setOnClickListener
                val newFile = File(context.cacheDir, "$newName.jpg")
                newFile.outputStream().use { bmp.compress(Bitmap.CompressFormat.JPEG, 100, it) }

                val newUri = Uri.fromFile(newFile).toString()
                imageUris[currentIndex] = newUri
                pagerAdapter.updateUriAt(currentIndex, newUri)
            }
            dialog.dismiss()
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == REQ_CROP) {
            val out = data?.getStringExtra("output_uri") ?: return
            val uri = Uri.parse(out)
            val bmp = decodeBitmapFromUri(uri, MAX_ORIGINAL) ?: return
            editedBitmaps[currentIndex] = bmp
            previewBitmaps[currentIndex] = getPreviewBitmap(bmp, MAX_PREVIEW)
            updatePageImage(bmp)
            if (::filterAdapter.isInitialized) filterAdapter.updateBaseBitmap(bmp)
        }
    }
}
