package com.app.figpdfconvertor.figpdf.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.text.Editable
import android.text.InputFilter
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.app.figpdfconvertor.figpdf.BuildConfig
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded
import com.app.figpdfconvertor.figpdf.ads.AdManagerRewarded.AdFinished
import com.app.figpdfconvertor.figpdf.databinding.ActivityInterviewBotQuestionBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogInterviewBotResultBinding
import com.app.figpdfconvertor.figpdf.api.ApiClient
import com.app.figpdfconvertor.figpdf.api.ApiService
import com.app.figpdfconvertor.figpdf.customwidget.VisualizerView
import com.app.figpdfconvertor.figpdf.model.InterviewResult
import com.app.figpdfconvertor.figpdf.model.SubmitAnswerRequest
import com.app.figpdfconvertor.figpdf.model.SubmitAnswerResponse
import com.app.figpdfconvertor.figpdf.preferences.AppHelper
import com.app.figpdfconvertor.figpdf.utils.MyUtils
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.toString

class InterviewBotQuestionActivity : BaseActivity() {

    private lateinit var binding: ActivityInterviewBotQuestionBinding
    private lateinit var questions: List<String>
    private var currentIndex = 0
    private lateinit var sessionId: String
    private val allResults = mutableListOf<InterviewResult>()
    private var dialogBinding: DialogInterviewBotResultBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityInterviewBotQuestionBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // If loading is showing, block exit
                showExitDialog(this@InterviewBotQuestionActivity)

            }
        })
        questions = intent.getStringArrayListExtra("questions") ?: emptyList()
        sessionId = intent.getStringExtra("session_id") ?: ""
        if (questions.isNotEmpty()) {
            binding.numbersofQue.max = questions.size
            showQuestion()
        }

        binding.txtAnswer.filters = arrayOf(InputFilter.LengthFilter(500))
        binding.txtAnswer.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val length = s?.length ?: 0
                binding.txtletterCount.text = "$length/500 characters"
                binding.txtAnswer.error =
                    if (length >= 500) "Maximum 500 characters allowed" else null
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        binding.imgVoice.setOnClickListener { openVoiceDialog() }
        binding.llSubmit.setOnClickListener { goToNextQuestion() }
        binding.txtSkip.setOnClickListener { goToNextQuestion() }
        binding.imgBack.setOnClickListener { finish() }
    }

    fun showResultDialog(context: Context) {
        dialogBinding = DialogInterviewBotResultBinding.inflate(LayoutInflater.from(context))

        val dialog = AlertDialog.Builder(context)
            .setView(dialogBinding!!.root)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        dialogBinding!!.viewResult.setOnClickListener {
            if (AppHelper.getShowRewardInterviewbotSubmit()) {
                AdManagerRewarded.showRewardedAd(this, AdFinished {
                    openResultActivity()
                })
            } else {
                openResultActivity()
            }

        }

        dialog.show()
    }

    private fun openVoiceDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_speech_input, null)
        val etSpeech = dialogView.findViewById<EditText>(R.id.etSpeechResult)
        val tvCount = dialogView.findViewById<TextView>(R.id.tvCount)
        val visualizerView = dialogView.findViewById<VisualizerView>(R.id.visualizerView)
        val imgClose = dialogView.findViewById<ImageView>(R.id.imgClose)

        val bottomSheet = BottomSheetDialog(this)
        bottomSheet.setContentView(dialogView)
        bottomSheet.setOnDismissListener {
            stopSpeechRecognizer()
        }
        bottomSheet.show()
        imgClose.setOnClickListener {
            val enteredText = etSpeech.text.toString().trim()
            if (enteredText.isNotEmpty()) {
                // Set text in your main screen TextInputEditText
                binding.txtAnswer.setText(enteredText)
            }

            // ⛔ Stop mic + visualizer
            stopSpeechRecognizer()
            visualizerView.stopVisualizer()

            bottomSheet.dismiss()
        }
        etSpeech.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                tvCount.text = "${s?.length ?: 0}/500 characters"
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        // ✅ Start speech recognizer & visualizer animation
        startSpeechToText(etSpeech, visualizerView)
    }

    private var speechRecognizer: SpeechRecognizer? = null
    private var recognizerIntent: Intent? = null
    private var isListening = false

    private fun startSpeechToText(etSpeech: EditText, visualizerView: VisualizerView) {
        if (speechRecognizer == null) {
            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        }

        if (recognizerIntent == null) {
            recognizerIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            }
        }

        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                isListening = true
            }

            override fun onBeginningOfSpeech() {}

            override fun onRmsChanged(rmsdB: Float) {
                val amplitude = (rmsdB.takeIf { it >= 0 } ?: 0f)
                visualizerView.updateAmplitude(amplitude)
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                isListening = false
                // 👉 Don’t restart here (causes Error 5)
            }

            override fun onError(error: Int) {
                Log.e("SpeechRecognizer", "Error: $error")

                isListening = false
                when (error) {
                    SpeechRecognizer.ERROR_NO_MATCH,
                    SpeechRecognizer.ERROR_SPEECH_TIMEOUT,
                        -> {
                        // safe retry
                        restartListening()
                    }

                    SpeechRecognizer.ERROR_CLIENT -> {
                        stopSpeechRecognizer() // full reset
                    }

                    else -> {
                        Toast.makeText(
                            this@InterviewBotQuestionActivity,
                            "Speech error: $error",
                            Toast.LENGTH_SHORT
                        ).show()
                        visualizerView.resetAmplitude()
                    }
                }
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                matches?.firstOrNull()?.let {
                    etSpeech.setText(it)
                    etSpeech.setSelection(it.length)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial =
                    partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                partial?.firstOrNull()?.let {
                    etSpeech.setText(it)
                    etSpeech.setSelection(it.length)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        restartListening()
    }

    private fun restartListening() {
        if (!isListening) {
            speechRecognizer?.cancel()
            recognizerIntent?.let { speechRecognizer?.startListening(it) }
            isListening = true
        }
    }

    private fun stopSpeechRecognizer() {
        try {
            speechRecognizer?.apply {
                stopListening()
                cancel()
                setRecognitionListener(null)
            }
            // Delay destroy to avoid ERROR_CLIENT
            speechRecognizer?.destroy()
        } catch (e: Exception) {
            Log.e("SpeechRecognizer", "stop error: ${e.localizedMessage}")
        }
        speechRecognizer = null
        recognizerIntent = null
        isListening = false
    }


    private suspend fun submitCandidateAnswer(
        sessionId: String,
        question: String,
        answer: String,
    ): SubmitAnswerResponse? {
        return try {
            val api = ApiClient.retrofit.create(ApiService::class.java)
            val response = api.submitCandidateAnswer(
                sessionId,
                BuildConfig.VERSION_CODE,
                SubmitAnswerRequest(question, answer)
            )
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            Log.e("InterviewBot", "Exception: ${e.localizedMessage}")
            null
        }
    }

    private fun showQuestion() {
        val question = questions[currentIndex]
        binding.txtQuestion.text = question
        binding.txtQueCount.text = "Question ${currentIndex + 1} of ${questions.size}"
        binding.numbersofQue.progress = currentIndex + 1
    }

    /* private fun goToNextQuestion() {
         val answer = binding.txtAnswer.text.toString().trim()
         val question = questions[currentIndex]

         lifecycleScope.launch {
             val response = submitCandidateAnswer(sessionId, question, answer)
             val result = InterviewResult(
                 question = question,
                 answer = answer,
                 score = response?.evaluation?.score ?: 0,
                 strengths = response?.evaluation?.strengths ?: "",
                 weaknesses = response?.evaluation?.weaknesses ?: "",
                 communicationToneFeedback = response?.evaluation?.communicationToneFeedback ?: "",
                 recommendation = response?.evaluation?.recommendation ?: ""
             )
             allResults.add(result)

             if (currentIndex < questions.size - 1) {
                 currentIndex++
                 showQuestion()
                 binding.txtAnswer.setText("")
             } else {
                 openResultActivity()
             }
         }
     }*/
    private fun goToNextQuestion() {
        val answer = binding.txtAnswer.text.toString().trim()
        val question = questions[currentIndex]

        lifecycleScope.launch {
            val response = submitCandidateAnswer(sessionId, question, answer)
            val result = InterviewResult(
                question = question,
                answer = answer,
                score = response?.evaluation?.score ?: 0,
                strengths = response?.evaluation?.strengths ?: "",
                weaknesses = response?.evaluation?.weaknesses ?: "",
                communicationToneFeedback = response?.evaluation?.communicationToneFeedback ?: "",
                recommendation = response?.evaluation?.recommendation ?: ""
            )

            // ✅ Log every question submission
            Log.e("InterviewBot", "Submitted Question: ${result.question}")
            Log.e("InterviewBot", "Answer: ${result.answer}")
            Log.e("InterviewBot", "Score: ${result.score}")
            Log.e("InterviewBot", "Strengths: ${result.strengths}")
            Log.e("InterviewBot", "Weaknesses: ${result.weaknesses}")
            Log.e("InterviewBot", "Tone Feedback: ${result.communicationToneFeedback}")
            Log.e("InterviewBot", "Recommendation: ${result.recommendation}")

            allResults.add(result)

            if (currentIndex < questions.size - 1) {
                currentIndex++
                showQuestion()
                binding.txtAnswer.setText("")
            } else {
                showResultDialog(this@InterviewBotQuestionActivity)
            }
        }
    }


    private fun openResultActivity() {
        val intent = Intent(this, InterviewBotResultActivity::class.java)
        intent.putExtra("interview_results", ArrayList(allResults))
        startActivity(intent)
        finish()
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

        if (exitDialog!!.window != null) {
            exitDialog!!.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        }

        cancelBtn.setOnClickListener(View.OnClickListener { v: View? -> exitDialog!!.dismiss() })

        okBtn.setOnClickListener(View.OnClickListener { v: View? ->
            finish()
            exitDialog!!.dismiss()
        })

        exitDialog!!.show()
    }
}

