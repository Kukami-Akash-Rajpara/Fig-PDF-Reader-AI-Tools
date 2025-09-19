package com.app.figpdfconvertor.figpdf.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivityInterviewBotMainBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogInterviewBotBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogNotTextErrorBinding
import com.app.figpdfconvertor.figpdf.utils.MyUtils

class InterviewBotMainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityInterviewBotMainBinding

    // keep reference to dialog binding
    private var dialogBinding: DialogInterviewBotBinding? = null
    private var pickedDocumentUri: Uri? = null
    private var dialogErrorBinding: DialogNotTextErrorBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityInterviewBotMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

                // Otherwise, behave normally
                finish()

            }
        })
        /*  Glide.with(this)
              .asGif() // ensures it's treated as a GIF
              .load(R.raw.my_gif) // or .load("file:///android_asset/my_gif.gif")
              .into(binding.imgBot)*/
        binding.imgBack.setOnClickListener {
            finish()
        }

        binding.llCandidate.setOnClickListener {
            startActivity(Intent(this, InterviewBatDataActivity::class.java))
        }
        val translateAnim = TranslateAnimation(
            0f, 0f,   // fromXDelta, toXDelta
            0f, -40f  // fromYDelta, toYDelta (move up by 40px)
        )
        translateAnim.duration = 1000
        translateAnim.repeatCount = Animation.INFINITE
        translateAnim.repeatMode = Animation.REVERSE

        binding.imageView.startAnimation(translateAnim)

        binding.rlHiring.setOnClickListener {
            startActivity(Intent(this, ComingSoonActivity::class.java))
        }
    }
}
