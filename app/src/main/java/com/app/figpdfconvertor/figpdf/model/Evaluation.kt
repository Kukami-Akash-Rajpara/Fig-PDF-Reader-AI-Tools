package com.app.figpdfconvertor.figpdf.model

import com.google.gson.annotations.SerializedName

data class Evaluation(
    val score: Int,
    val strengths: String,
    val weaknesses: String,
    @SerializedName("communication_tone_feedback")
    val communicationToneFeedback: String,
    val recommendation: String
)