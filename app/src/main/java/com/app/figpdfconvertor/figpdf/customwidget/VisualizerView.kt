package com.app.figpdfconvertor.figpdf.customwidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.sin

class VisualizerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint().apply {
        color = 0xFFBF496A.toInt()
        style = Paint.Style.STROKE
        strokeWidth = 6f
        isAntiAlias = true
    }

    private var amplitude: Float = 0f
    private var phase: Float = 0f

    fun updateAmplitude(value: Float) {
        amplitude = value.coerceIn(0f, 200f)
        invalidate()
    }

    fun stopVisualizer() {
        amplitude = 0f
        invalidate()
    }

    fun resetAmplitude() {
        amplitude = 0f
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val path = Path()
        val width = width.toFloat()
        val height = height.toFloat()
        val centerY = height / 2f

        val waveLength = width / 1.5f
        phase += 0.15f

        path.moveTo(0f, centerY)

        var x = 0f
        while (x < width) {
            val y = (centerY +
                    amplitude * sin((2.0 * Math.PI * (x / waveLength) + phase)).toFloat())
            path.lineTo(x, y)
            x += 15f
        }

        canvas.drawPath(path, paint)
    }
}

