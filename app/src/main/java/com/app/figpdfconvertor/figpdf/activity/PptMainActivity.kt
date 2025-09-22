package com.app.figpdfconvertor.figpdf.activity

import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.BaseActivity
import com.app.figpdfconvertor.figpdf.databinding.ActivityPptMainBinding
import com.app.figpdfconvertor.figpdf.utils.MyUtils


class PptMainActivity : BaseActivity() {
    private lateinit var binding: ActivityPptMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MyUtils.fullScreenLightStatusBar(this)

        binding = ActivityPptMainBinding.inflate(layoutInflater)
        setContentView(binding.getRoot())

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

    }
}