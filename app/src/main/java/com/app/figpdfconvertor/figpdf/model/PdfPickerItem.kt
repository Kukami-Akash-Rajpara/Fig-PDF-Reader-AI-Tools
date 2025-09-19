package com.app.figpdfconvertor.figpdf.model

import android.net.Uri

data class PdfPickerItem(
    var uri: Uri? = null,
    var name: String = "Select PDF"
)
