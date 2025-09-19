package com.app.figpdfconvertor.figpdf.model

data class SubmitAnswerResponse(
    val session_id: String,
    val question: String,
    val answer: String,
    val evaluation: Evaluation?,
    val message: String
)
