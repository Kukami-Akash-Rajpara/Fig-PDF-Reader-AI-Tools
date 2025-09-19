package com.app.figpdfconvertor.figpdf.model.ocr;

import com.google.gson.annotations.SerializedName;

public class PostResponse {
    @SerializedName("job_id")
    private String jobId;

    @SerializedName("status")
    private String status;

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }
}
