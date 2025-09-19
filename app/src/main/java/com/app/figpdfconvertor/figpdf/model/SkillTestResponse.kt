package com.app.figpdfconvertor.figpdf.model

data class SkillTestResponse(
    val session_id: String,
    val position_level: String,
    val difficulty: String,
    val questions: Questions,
    val message: String
)

data class Questions(
    val interview_questions: List<String>,
    val question_breakdown: QuestionBreakdown,
    val message: String
)

data class QuestionBreakdown(
    val technical: String,
    val managerial: String,
    val behavioral: String
)
