package com.app.figpdfconvertor.figpdf.model.ocr;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class JobResult {
    @SerializedName("text")
    private Map<String, String> text;

    @SerializedName("session_id")
    private String sessionId;

    public Map<String, String> getText() {
        return text;
    }

    public String getSessionId() {
        return sessionId;
    }
}
