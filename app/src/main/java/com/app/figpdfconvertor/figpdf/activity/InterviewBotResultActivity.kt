package com.app.figpdfconvertor.figpdf.activity

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.ActivityInterviewBotResultBinding
import com.app.figpdfconvertor.figpdf.adapter.InterviewResultAdapter
import com.app.figpdfconvertor.figpdf.model.InterviewResult
import com.app.figpdfconvertor.figpdf.utils.MyUtils

class InterviewBotResultActivity : BaseActivity() {

    private lateinit var binding: ActivityInterviewBotResultBinding
    private val results = mutableListOf<InterviewResult>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)
        binding = ActivityInterviewBotResultBinding.inflate(layoutInflater)
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val data = intent.getSerializableExtra("interview_results") as? ArrayList<InterviewResult>
        data?.let { results.addAll(it) }

        val adapter = InterviewResultAdapter(results)
        binding.rvResult.layoutManager = LinearLayoutManager(this)
        binding.rvResult.adapter = adapter

        binding.imgBack.setOnClickListener {
            finish()
        }
    }
}
