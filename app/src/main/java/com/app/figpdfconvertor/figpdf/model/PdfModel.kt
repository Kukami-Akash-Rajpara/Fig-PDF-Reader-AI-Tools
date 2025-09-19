package com.app.figpdfconvertor.figpdf.model

import android.net.Uri

data class PdfModel(
    val name: String,
    val uri: Uri,
    var isSelected: Boolean = false
)
