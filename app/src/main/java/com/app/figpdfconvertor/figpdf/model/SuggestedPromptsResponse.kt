package com.app.figpdfconvertor.figpdf.model


data class SuggestedPromptsResponse(
    val suggested_prompts: List<String>
)

data class TemplateResponse(
    val templates: Map<String, List<TemplateItem>>,
    val graphs: Map<String, List<TemplateItem>>
)

data class TemplateItem(
    val template_id: String,
    val image_url: String
)
