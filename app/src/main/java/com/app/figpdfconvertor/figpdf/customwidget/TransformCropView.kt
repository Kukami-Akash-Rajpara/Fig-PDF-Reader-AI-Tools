package com.app.figpdfconvertor.figpdf.customwidget

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import androidx.appcompat.widget.AppCompatImageView
import kotlin.math.*

/**
 * Pan/zoom/rotate the image. Crop frame stays fixed in the center.
 * On export, we render exactly what's under the crop frame into a new bitmap.
 */
class TransformCropView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : AppCompatImageView(context, attrs) {

    // Current image transform (we don't use ImageView's internal matrix)
    private val contentMatrix = Matrix()
    private val inverseMatrix = Matrix()

    // Gestures
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    private val gestureDetector = GestureDetector(context, GestureListener())

    // Two-finger rotation
    private var isRotating = false
    private var lastAngle = 0f

    // Pan
    private var lastX = 0f
    private var lastY = 0f
    private var isPanning = false

    // Crop frame (centered); we keep it in VIEW coords
    private val cropRect = RectF()
    private var aspectW = 0     // 0 = free
    private var aspectH = 0

    // Overlay paints
    private val dimPaint = Paint().apply { color = 0x99000000.toInt() }
    private val framePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 5f
    }

    // temp rects/paths
    private val viewRect = RectF()
    private val path = Path()

    init {
        scaleType = ScaleType.MATRIX
        isClickable = true
        isFocusable = true
        setWillNotDraw(false)
    }

    // --- Public API ---

    fun setAspectRatio(w: Int, h: Int) {
        aspectW = w
        aspectH = h
        post { layoutCropRect() }
        invalidate()
    }

    fun setFreeAspect() {
        setAspectRatio(0, 0)
    }

    fun rotate90() {
        val cx = width / 2f
        val cy = height / 2f
        contentMatrix.postRotate(90f, cx, cy)
        invalidate()
    }

    fun reset() {
        contentMatrix.reset()
        post { fitImageToView() }
        invalidate()
    }

    /** Export the area under the crop rect to a new bitmap (rotation/scale respected). */
    fun getCroppedBitmap(): Bitmap? {
        val drawableBmp = (drawable as? BitmapDrawable)?.bitmap ?: return null
        if (width == 0 || height == 0) return null

        // Output size = crop rect size in pixels (view space)
        val outW = cropRect.width().roundToInt().coerceAtLeast(1)
        val outH = cropRect.height().roundToInt().coerceAtLeast(1)
        val output = Bitmap.createBitmap(outW, outH, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)

        // Build a matrix that draws the image such that the cropRect aligns to (0,0,outW,outH)
        val drawMatrix = Matrix(contentMatrix)
        // shift so that cropRect's top-left is at 0,0
        drawMatrix.postTranslate(-cropRect.left, -cropRect.top)

        canvas.concat(drawMatrix)
        canvas.drawBitmap(drawableBmp, 0f, 0f, null)
        return output
    }

    // --- Layout ---

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        viewRect.set(0f, 0f, w.toFloat(), h.toFloat())
        layoutCropRect()
        fitImageToView()
    }

    private fun layoutCropRect() {
        if (width == 0 || height == 0) return
        val vw = width.toFloat()
        val vh = height.toFloat()

        val margin = min(vw, vh) * 0.07f
        var cw = vw - margin * 2
        var ch = vh - margin * 2

        if (aspectW > 0 && aspectH > 0) {
            val target = cw / ch
            val ar = aspectW.toFloat() / aspectH
            if (target > ar) {
                // too wide → limit by height
                cw = ch * ar
            } else {
                // too tall → limit by width
                ch = cw / ar
            }
        }

        val left = (vw - cw) / 2f
        val top = (vh - ch) / 2f
        cropRect.set(left, top, left + cw, top + ch)
    }

    private fun fitImageToView() {
        val bmp = (drawable as? BitmapDrawable)?.bitmap ?: return
        contentMatrix.reset()

        // Fit center
        val vw = width.toFloat()
        val vh = height.toFloat()
        val bw = bmp.width.toFloat()
        val bh = bmp.height.toFloat()

        val scale = min(vw / bw, vh / bh)
        val dx = (vw - bw * scale) / 2f
        val dy = (vh - bh * scale) / 2f

        contentMatrix.postScale(scale, scale)
        contentMatrix.postTranslate(dx, dy)
        imageMatrix = contentMatrix
    }

    // --- Drawing ---

    override fun onDraw(canvas: Canvas) {
        // Draw the bitmap with our matrix
        imageMatrix = contentMatrix
        super.onDraw(canvas)

        // Dim outside the crop rect
        path.reset()
        path.addRect(viewRect, Path.Direction.CW)
        path.addRect(cropRect, Path.Direction.CCW)
        canvas.drawPath(path, dimPaint)

        // Frame
        canvas.drawRect(cropRect, framePaint)
        drawRuleOfThirds(canvas)
    }

    private fun drawRuleOfThirds(canvas: Canvas) {
        val stepX = cropRect.width() / 3f
        val stepY = cropRect.height() / 3f
        for (i in 1..2) {
            val x = cropRect.left + i * stepX
            canvas.drawLine(x, cropRect.top, x, cropRect.bottom, framePaint)
            val y = cropRect.top + i * stepY
            canvas.drawLine(cropRect.left, y, cropRect.right, y, framePaint)
        }
    }

    // --- Touch handling ---

    override fun onTouchEvent(event: MotionEvent): Boolean {
        if (drawable == null) return false

        scaleDetector.onTouchEvent(event)
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                isPanning = true
                lastX = event.x
                lastY = event.y
                parent?.requestDisallowInterceptTouchEvent(true)
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                if (event.pointerCount == 2) {
                    isRotating = true
                    lastAngle = angleBetween(event)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                if (isRotating && event.pointerCount == 2) {
                    val angle = angleBetween(event)
                    val delta = angle - lastAngle
                    lastAngle = angle
                    contentMatrix.postRotate(delta, width / 2f, height / 2f)
                    invalidate()
                } else if (isPanning) {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    lastX = event.x
                    lastY = event.y
                    contentMatrix.postTranslate(dx, dy)
                    invalidate()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> {
                if (event.pointerCount == 2) isRotating = false
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                isPanning = false
                isRotating = false
                parent?.requestDisallowInterceptTouchEvent(false)
            }
        }
        return true
    }

    private fun angleBetween(ev: MotionEvent): Float {
        if (ev.pointerCount < 2) return 0f
        val dx = (ev.getX(1) - ev.getX(0)).toDouble()
        val dy = (ev.getY(1) - ev.getY(0)).toDouble()
        return Math.toDegrees(atan2(dy, dx)).toFloat()
    }

    private inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            contentMatrix.postScale(
                detector.scaleFactor, detector.scaleFactor,
                detector.focusX, detector.focusY
            )
            invalidate()
            return true
        }
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        override fun onDoubleTap(e: MotionEvent): Boolean {
            // quick smart-zoom to fit again
            reset()
            return true
        }
    }
}
