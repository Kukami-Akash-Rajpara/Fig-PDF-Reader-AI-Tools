package com.app.figpdfconvertor.figpdf.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.app.figpdfconvertor.figpdf.BuildConfig
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded.AdFinished
import com.app.figpdfconvertor.figpdf.api.ApiClient
import com.app.figpdfconvertor.figpdf.databinding.ActivityInterviewBatDataBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogNotTextErrorBinding
import com.app.figpdfconvertor.figpdf.preferences.AppHelper
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class InterviewBatDataActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInterviewBatDataBinding
    private lateinit var filePickerLauncher: ActivityResultLauncher<Intent>
    private var pickedDocumentUri: Uri? = null
    private var dialogErrorBinding: DialogNotTextErrorBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityInterviewBatDataBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.llLoading.visibility == View.VISIBLE) {
                    // If loading is showing, block exit
                    showExitDialog(this@InterviewBatDataActivity)
                } else {
                    // Otherwise, behave normally
                    finish()
                }
            }
        })
        filePickerLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val uri: Uri? = result.data?.data
                uri?.let {
                    val fileName = getFileNameFromUri(it)
                    // ✅ update dialog binding, not activity binding
                    binding?.txtFileName?.text = fileName
                    pickedDocumentUri = it
                }
            }
        }
        binding!!.llChooseFile.setOnClickListener {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/pdf" // or "application/pdf"
            }
            filePickerLauncher.launch(intent)
        }

        // Default selection
        binding.imgExtentionDrop.setOnClickListener { v ->
            showCustomPopup(v, menuRes = R.menu.menu_position) { selected ->
                binding.etPosition.setText(selected)
            }
        }

        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.imgDifficultyDrop.setOnClickListener { v ->
            showCustomPopup(v, menuRes = R.menu.menu_difficulty) { selected ->
                binding.txtQuestionDifficulty.setText(selected)
            }
        }

        binding.imgRangeDrop.setOnClickListener { v ->
            val ranges = (10..100 step 10).map { "1-$it" }
            showCustomPopup(v, items = ranges) { selected ->
                binding.txtQueRange.text = selected
            }
        }

        binding!!.okButton.setOnClickListener {
            val position = binding!!.etPosition.text.toString().trim().lowercase()
            val difficulty = binding!!.txtQuestionDifficulty.text.toString().lowercase()
            val range = binding!!.txtQueRange.text.toString()

            // ✅ Extract last number from range like "1-50" → 50
            val numQuestions = range.split("-").lastOrNull()?.toIntOrNull() ?: 10

            if (pickedDocumentUri == null) {
                Toast.makeText(this, "Please choose a resume file", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Send the path of this new file to next activity


            // Now call API with extracted value
            callGenerateQuestionsApi(
                pickedDocumentUri!!,
                position,
                difficulty,
                numQuestions
            )
        }
    }

    private fun callGenerateQuestionsApi(
        fileUri: Uri,
        position: String,
        difficulty: String,
        numQuestions: Int,
    ) {

        val file = File(cacheDir, getFileNameFromUri(fileUri))
        contentResolver.openInputStream(fileUri)?.use { input ->
            file.outputStream().use { output -> input.copyTo(output) }
        }

        val requestFile = file.asRequestBody("application/pdf".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("resume", file.name, requestFile)

        val positionPart = position.toRequestBody("text/plain".toMediaTypeOrNull())
        val difficultyPart = difficulty.toRequestBody("text/plain".toMediaTypeOrNull())
        val numPart = numQuestions.toString().toRequestBody("text/plain".toMediaTypeOrNull())

        val appVersion = BuildConfig.VERSION_CODE

        lifecycleScope.launch {
            try {
                showLoading(true) // 👈 show loading view
                showMain(false) // 👈 show loading view

                /*  val response = apiService.generateQuestions(
                      appVersion,
                      body,
                      positionPart,
                      difficultyPart,
                      numPart
                  )*/

                val response = ApiClient.apiService.generateQuestions(
                    appVersion,
                    body,
                    positionPart,
                    difficultyPart,
                    numPart)

                Log.d("API_RESPONSE", "Raw response: ${response.raw()}")
                Log.d("API_RESPONSE", "Code: ${response.code()} Message: ${response.message()}")

                if (response.isSuccessful) {
                    val data = response.body()
                    Log.d("API_RESPONSE", "Body: $data")

                    data?.let {
                        /*  Toast.makeText(this@InterviewBotMainActivity, it.message, Toast.LENGTH_LONG)
                              .show()*/

                        if (it.questions?.interview_questions?.isNotEmpty() == true) {
                            val intent = Intent(
                                this@InterviewBatDataActivity,
                                InterviewBotQuestionActivity::class.java
                            )
                            intent.putStringArrayListExtra(
                                "questions",
                                ArrayList(it.questions.interview_questions)
                            )
                            intent.putExtra("session_id", it.session_id)
                            startActivity(intent)
                            finish()
                        } else {
                            showErrorDialog("No questions generated! Please try again with another resume.")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("API_RESPONSE", "Error body: $errorBody")

                    // ✅ Parse API error
                    if (!errorBody.isNullOrEmpty() && errorBody.contains("Failed to extract text")) {
                        showErrorDialog("We could not extract text from the uploaded PDF. Please upload a valid resume.")
                    } else {
                        showErrorDialog("❌ Error: ${response.code()} - Something went wrong.")
                    }
                }

            } catch (e: Exception) {
                Log.e("API_RESPONSE", "Exception: ${e.localizedMessage}", e)
                Toast.makeText(
                    this@InterviewBatDataActivity,
                    "⚠ Error: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                showLoading(false) // 👈 hide loading view
                showMain(true) // 👈 hide loading view
            }
        }
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
    private fun showErrorDialog(message: String) {
        dialogErrorBinding = DialogNotTextErrorBinding.inflate(LayoutInflater.from(this))

        val dialog = AlertDialog.Builder(this)
            .setView(dialogErrorBinding!!.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogErrorBinding!!.llOk.setOnClickListener {
            dialog.dismiss()
        }
        dialogErrorBinding!!.txtError.text = message
        dialog.show()
    }

    private fun getFileNameFromUri(uri: Uri): String {
        var name = "Unknown"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIndex >= 0) {
                name = cursor.getString(nameIndex)
            }
        }
        return name
    }

    private fun showLoading(show: Boolean) {
        binding.llLoading.visibility = if (show) View.VISIBLE else View.GONE
        if (!show) {
            // ✅ dismiss exit dialog if it's still open
            exitDialog?.dismiss()
            exitDialog = null
        }
    }

    private fun showMain(show: Boolean) {
        binding.llMain.visibility = if (show) View.VISIBLE else View.GONE
    }

    private fun showCustomPopup(
        anchor: View,
        menuRes: Int? = null,
        items: List<String>? = null,
        onItemClick: (String) -> Unit
    ) {
        val inflater = LayoutInflater.from(this)
        val popupView = inflater.inflate(R.layout.popup_menu_custom, null)
        val container = popupView.findViewById<LinearLayout>(R.id.menuContainer)

        // Add items
        if (menuRes != null) {
            val tempMenu = PopupMenu(this, anchor)
            tempMenu.menuInflater.inflate(menuRes, tempMenu.menu)
            for (i in 0 until tempMenu.menu.size()) {
                val item = tempMenu.menu.getItem(i)
                addItemToContainer(container, item.title.toString())
                if (i < tempMenu.menu.size() - 1) addDivider(container)
            }
        }
        items?.forEachIndexed { index, title ->
            addItemToContainer(container, title)
            if (index < items.size - 1) addDivider(container)
        }

        val popupWindow = PopupWindow(
            popupView,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.isOutsideTouchable = true
        popupWindow.elevation = 16f

        // Set click listeners that dismiss popup
        for (i in 0 until container.childCount) {
            val child = container.getChildAt(i)
            if (child is TextView) {
                child.setOnClickListener {
                    onItemClick(child.text.toString())
                    popupWindow.dismiss()
                }
            }
        }

        // ✅ Measure popup content width and height
        popupView.measure(
            View.MeasureSpec.UNSPECIFIED,
            View.MeasureSpec.UNSPECIFIED
        )
        val popupWidth = popupView.measuredWidth
        val popupHeight = popupView.measuredHeight

        // ✅ Get anchor location
        val location = IntArray(2)
        anchor.getLocationOnScreen(location)
        val anchorX = location[0]
        val anchorY = location[1]
        val anchorWidth = anchor.width
        val anchorHeight = anchor.height

        // ✅ Show popup aligned to bottom-end of anchor
        popupWindow.showAtLocation(
            anchor,
            0,
            anchorX + anchorWidth - popupWidth, // X offset
            anchorY + anchorHeight // Y offset (just below anchor)
        )
    }


    private fun addItemToContainer(container: LinearLayout, title: String) {
        val tv = TextView(this).apply {
            text = title
            setPadding(40, 20, 40, 20)
            setTextColor(resources.getColor(R.color.black, null))
            textSize = 15f
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, // ✅ wrap content
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(tv)
    }

    private fun addDivider(container: LinearLayout) {
        val divider = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                1
            )
            setBackgroundColor(Color.parseColor("#DDDDDD"))
        }
        container.addView(divider)
    }

}