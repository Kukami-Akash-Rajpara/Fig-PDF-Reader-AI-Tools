package com.app.figpdfconvertor.figpdf.model

import android.graphics.Bitmap
import java.io.File

data class PdfFileItem(
    var file: File,
    var name: String,
    val thumbnail: Bitmap?,
    val createdTime: Long,
    val size: Long
)
