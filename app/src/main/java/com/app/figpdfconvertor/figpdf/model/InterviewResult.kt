package com.app.figpdfconvertor.figpdf.model

import java.io.Serializable

data class InterviewResult(
    val question: String,
    val answer: String,
    val score: Int = 0,
    val strengths: String = "",
    val weaknesses: String = "",
    val communicationToneFeedback: String = "",
    val recommendation: String = ""
) : Serializable
