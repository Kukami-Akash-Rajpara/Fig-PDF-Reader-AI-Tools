package com.app.figpdfconvertor.figpdf.customwidget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class CropOverlayView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val borderPaint = Paint().apply {
        color = Color.WHITE
        strokeWidth = 5f
        style = Paint.Style.STROKE
    }

    private val frame = RectF(200f, 200f, 600f, 600f) // initial square

    private var lastX = 0f
    private var lastY = 0f
    private var dragging = false

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawRect(frame, borderPaint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                if (frame.contains(event.x, event.y)) {
                    dragging = true
                    lastX = event.x
                    lastY = event.y
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (dragging) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    frame.offset(dx, dy)
                    invalidate()
                    lastX = event.x
                    lastY = event.y
                }
            }
            MotionEvent.ACTION_UP -> dragging = false
        }
        return true
    }

    fun getCropRect(): RectF = frame
}
