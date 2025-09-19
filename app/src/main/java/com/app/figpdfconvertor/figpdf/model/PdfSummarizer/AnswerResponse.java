package com.app.figpdfconvertor.figpdf.model.PdfSummarizer;

import com.google.gson.annotations.SerializedName;

public class AnswerResponse {

    @SerializedName("answer")
    private String answer;

    @SerializedName("conversation_id")
    private String conversationId;

    @SerializedName("language")
    private String language;

    public String getAnswer() {
        return answer;
    }

    public String getConversationId() {
        return conversationId;
    }

    public String getLanguage() {
        return language;
    }
}