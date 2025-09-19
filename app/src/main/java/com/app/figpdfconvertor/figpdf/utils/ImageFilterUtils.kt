package com.app.figpdfconvertor.figpdf.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale


object ImageFilterUtils {

    /*fun applyFilter(bitmap: Bitmap, filter: String): Bitmap {
        val paint = Paint()
        val cm = ColorMatrix()

        when (filter) {
            "Gray" -> cm.setSaturation(0f)

            "Sepia" -> cm.set(floatArrayOf(
                0.393f, 0.769f, 0.189f, 0f, 0f,
                0.349f, 0.686f, 0.168f, 0f, 0f,
                0.272f, 0.534f, 0.131f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))

            "Invert" -> cm.set(floatArrayOf(
                -1f, 0f, 0f, 0f, 255f,
                0f, -1f, 0f, 0f, 255f,
                0f, 0f, -1f, 0f, 255f,
                0f, 0f, 0f, 1f, 0f
            ))

            "Brightness" -> {
                val scale = 1f
                val translate = 50f
                cm.set(floatArrayOf(
                    scale, 0f, 0f, 0f, translate,
                    0f, scale, 0f, 0f, translate,
                    0f, 0f, scale, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
            }

            "Contrast" -> {
                val contrast = 1.5f
                val translate = (-0.5f * contrast + 0.5f) * 255f
                cm.set(floatArrayOf(
                    contrast, 0f, 0f, 0f, translate,
                    0f, contrast, 0f, 0f, translate,
                    0f, 0f, contrast, 0f, translate,
                    0f, 0f, 0f, 1f, 0f
                ))
            }

            "Saturation" -> cm.setSaturation(2f)

            "Vintage" -> cm.set(floatArrayOf(
                0.9f, 0.5f, 0.1f, 0f, 0f,
                0.3f, 0.8f, 0.2f, 0f, 10f,
                0.2f, 0.3f, 0.7f, 0f, 20f,
                0f, 0f, 0f, 1f, 0f
            ))

            "Cool" -> cm.set(floatArrayOf(
                1f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 1.2f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))

            "Warm" -> cm.set(floatArrayOf(
                1.2f, 0f, 0f, 0f, 0f,
                0f, 1f, 0f, 0f, 0f,
                0f, 0f, 0.8f, 0f, 0f,
                0f, 0f, 0f, 1f, 0f
            ))

            else -> return bitmap
        }

        paint.colorFilter = ColorMatrixColorFilter(cm)

        val config = bitmap.config ?: Bitmap.Config.ARGB_8888
        val filteredBitmap = createBitmap(bitmap.width, bitmap.height, config)
        val canvas = Canvas(filteredBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        return filteredBitmap
    }*/


    fun applyFilter(src: Bitmap, filterType: String): Bitmap {
        val result = createBitmap(src.getWidth(), src.getHeight(), src.getConfig()!!)
        val canvas = Canvas(result)
        val paint = Paint()
        val cm = ColorMatrix()

        when (filterType) {
            "Original" -> cm.reset()
            "Docs" -> {
                cm.setSaturation(0f)
                cm.set(
                    floatArrayOf(
                        2f, 0f, 0f, 0f, -255f,
                        0f, 2f, 0f, 0f, -255f,
                        0f, 0f, 2f, 0f, -255f,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
            }

            "Image" -> {
                val enhance = ColorMatrix()
                enhance.set(
                    floatArrayOf(
                        1.1f, 0f, 0f, 0f, 10f,   // Red ↑
                        0f, 1.1f, 0f, 0f, 10f,   // Green ↑
                        0f, 0f, 1.1f, 0f, 10f,   // Blue ↑
                        0f, 0f, 0f, 1f, 0f     // Alpha
                    )
                )
                cm.postConcat(enhance)
            }

            "Super" -> cm.set(
                floatArrayOf(
                    1.3f, 0f, 0f, 0f, 0f,
                    0f, 1.3f, 0f, 0f, 0f,
                    0f, 0f, 1.3f, 0f, 0f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            "Enhance" -> cm.set(
                floatArrayOf(
                    1.1f, 0f, 0f, 0f, 10f,
                    0f, 1.1f, 0f, 0f, 10f,
                    0f, 0f, 1.1f, 0f, 10f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            "Enhance2" -> cm.set(
                floatArrayOf(
                    1.2f, 0f, 0f, 0f, 15f,
                    0f, 1.2f, 0f, 0f, 15f,
                    0f, 0f, 1.2f, 0f, 15f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

            "B&W" -> {
                cm.setSaturation(0f)
                val contrast = ColorMatrix()
                val scale = 1.5f // higher contrast
                val translate = (-0.5f * 255f) * (scale - 1f)
                contrast.set(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrast)
            }

            "B&W2" -> {
                cm.setSaturation(0f) // remove colors
                val contrast = ColorMatrix()
                val scale = 0.8f // lower contrast than B&W
                val translate = (1 - scale) * 128f // shift midpoint to keep brightness
                contrast.set(
                    floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                )
                cm.postConcat(contrast)
            }

            "Gray" -> cm.setSaturation(0f)
            "Invert" -> cm.set(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )

        }

        paint.setColorFilter(ColorMatrixColorFilter(cm))
        canvas.drawBitmap(src, 0f, 0f, paint)

        return result
    }


    fun applyContrast(src: Bitmap, contrast: Float): Bitmap {
        // contrast: 1.0 = normal, <1 = less, >1 = more
        val cm = ColorMatrix(
            floatArrayOf(
                contrast, 0f, 0f, 0f, 128f * (1 - contrast),
                0f, contrast, 0f, 0f, 128f * (1 - contrast),
                0f, 0f, contrast, 0f, 128f * (1 - contrast),
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(src, cm)
    }

    fun applyBrightness(src: Bitmap, value: Float): Bitmap {
        val cm = ColorMatrix()
        cm.set(
            floatArrayOf(
                1f, 0f, 0f, 0f, value,
                0f, 1f, 0f, 0f, value,
                0f, 0f, 1f, 0f, value,
                0f, 0f, 0f, 1f, 0f
            )
        )
        return applyColorMatrix(src, cm)
    }

    fun applyHDR(src: Bitmap, strength: Float): Bitmap {
        val blurred = Bitmap.createScaledBitmap(src, src.width / 2, src.height / 2, true)
        val scaledBack = blurred.scale(src.width, src.height)
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val bmp = createBitmap(src.width, src.height, config)
        val canvas = Canvas(bmp)

        val paint = Paint()
        // Blend original - blurred * strength → detail enhancement
        val cm = ColorMatrix(
            floatArrayOf(
                1f + strength, 0f, 0f, 0f, -128f * strength,
                0f, 1f + strength, 0f, 0f, -128f * strength,
                0f, 0f, 1f + strength, 0f, -128f * strength,
                0f, 0f, 0f, 1f, 0f
            )
        )
        paint.colorFilter = ColorMatrixColorFilter(cm)

        canvas.drawBitmap(src, 0f, 0f, null)
        canvas.drawBitmap(scaledBack, 0f, 0f, paint)

        return bmp
    }

    fun applySharpness(src: Bitmap, strength: Float): Bitmap {
        // strength = 0f → no change
        // strength = 1f → normal sharpening
        // strength > 1f → extra sharp

        val width = src.width
        val height = src.height
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val result = createBitmap(width, height, config)

        // Step 1: create blurred version (downscale → upscale trick)
        val blurred = src.scale(width / 2, height / 2)
        val scaledBack = blurred.scale(width, height)

        val pixelsSrc = IntArray(width * height)
        val pixelsBlur = IntArray(width * height)
        src.getPixels(pixelsSrc, 0, width, 0, 0, width, height)
        scaledBack.getPixels(pixelsBlur, 0, width, 0, 0, width, height)

        val pixelsOut = IntArray(width * height)

        for (i in pixelsSrc.indices) {
            val c = pixelsSrc[i]
            val b = pixelsBlur[i]

            val r = ((Color.red(c) + strength * (Color.red(c) - Color.red(b))).coerceIn(
                0f,
                255f
            )).toInt()
            val g = ((Color.green(c) + strength * (Color.green(c) - Color.green(b))).coerceIn(
                0f,
                255f
            )).toInt()
            val bC = ((Color.blue(c) + strength * (Color.blue(c) - Color.blue(b))).coerceIn(
                0f,
                255f
            )).toInt()

            pixelsOut[i] = Color.argb(Color.alpha(c), r, g, bC)
        }

        result.setPixels(pixelsOut, 0, width, 0, 0, width, height)
        return result
    }


    fun applyColorMatrix(src: Bitmap, cm: ColorMatrix): Bitmap {
        val config = src.config ?: Bitmap.Config.ARGB_8888
        val bmp = createBitmap(src.width, src.height, config)
        val canvas = Canvas(bmp)
        val paint = Paint()
        paint.colorFilter = ColorMatrixColorFilter(cm)
        canvas.drawBitmap(src, 0f, 0f, paint)
        return bmp
    }

}
