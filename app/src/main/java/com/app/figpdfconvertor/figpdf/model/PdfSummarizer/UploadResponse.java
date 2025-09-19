package com.app.figpdfconvertor.figpdf.model.PdfSummarizer;

import com.google.gson.annotations.SerializedName;

public class UploadResponse {

    @SerializedName("chunk_count")
    private int chunkCount;

    @SerializedName("original_language")
    private String originalLanguage;

    @SerializedName("filename")
    private String filename;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("message")
    private String message;

    @SerializedName("status")
    private String status;

    public int getChunkCount() {
        return chunkCount;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public String getFilename() {
        return filename;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }
}