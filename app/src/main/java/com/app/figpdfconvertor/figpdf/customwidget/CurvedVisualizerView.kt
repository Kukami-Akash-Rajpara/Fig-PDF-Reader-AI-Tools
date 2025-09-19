package com.app.figpdfconvertor.figpdf.customwidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class CurvedVisualizerView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paints: List<Paint> = listOf(
        Paint().apply { color = Color.MAGENTA; style = Paint.Style.STROKE; strokeWidth = 6f; alpha = 255; isAntiAlias = true },
        Paint().apply { color = Color.MAGENTA; style = Paint.Style.STROKE; strokeWidth = 5f; alpha = 120; isAntiAlias = true },
        Paint().apply { color = Color.MAGENTA; style = Paint.Style.STROKE; strokeWidth = 4f; alpha = 60; isAntiAlias = true }
    )

    private var amplitude = 1f
    private var phase = 0f
    private val path = Path()

    private val waveAnimator = object : Runnable {
        override fun run() {
            phase += 0.15f
            invalidate()
            postDelayed(this, 16) // ~60fps
        }
    }

    init {
        post(waveAnimator)
    }

    fun updateAmplitude(level: Float) {
        amplitude = level.coerceIn(0.2f, 3.0f)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val cx = width / 2f
        val cy = height.toFloat()
        val radius = (width / 2f) - 20f
        val points = 80

        for ((index, paint) in paints.withIndex()) {
            path.reset()
            for (i in 0..points) {
                val angle = 180 + (i * 180f / points) // Arc 180° to 360°
                val rad = Math.toRadians(angle.toDouble())
                val x = (cx + radius * cos(rad)).toFloat()
                val y = (cy + radius * sin(rad)).toFloat()

                // Wave offset for layered effect
                val offsetY = (sin(i * 0.3f + phase + index * 0.5f) * amplitude * 20)
                val finalY = y + offsetY

                if (i == 0) path.moveTo(x, finalY)
                else path.lineTo(x, finalY)
            }
            canvas.drawPath(path, paint)
        }
    }
}
