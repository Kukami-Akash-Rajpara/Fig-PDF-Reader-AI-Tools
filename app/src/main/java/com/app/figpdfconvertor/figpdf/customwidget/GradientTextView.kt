package com.app.figpdfconvertor.figpdf.customwidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView
import com.app.figpdfconvertor.figpdf.R

class GradientTextView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatTextView(context, attrs, defStyleAttr) {

    private var startColor: Int = Color.BLACK
    private var endColor: Int = Color.BLACK
    private var gradientShader: LinearGradient? = null

    init {
        context.theme.obtainStyledAttributes(attrs, R.styleable.GradientTextView, 0, 0).apply {
            try {
                startColor = getColor(R.styleable.GradientTextView_startColor, Color.BLACK)
                endColor = getColor(R.styleable.GradientTextView_endColor, Color.BLACK)
            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        gradientShader = LinearGradient(
            0f, 0f, width.toFloat(), 0f, // left → right
            startColor, endColor,
            Shader.TileMode.CLAMP
        )
    }

    override fun onDraw(canvas: Canvas) {
        paint.shader = gradientShader
        super.onDraw(canvas)
    }
}
