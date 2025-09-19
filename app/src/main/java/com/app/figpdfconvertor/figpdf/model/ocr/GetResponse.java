package com.app.figpdfconvertor.figpdf.model.ocr;

import com.google.gson.annotations.SerializedName;

public class GetResponse {
    @SerializedName("job_id")
    private String jobId;

    @SerializedName("status")
    private String status;

    @SerializedName("position_in_queue")
    private int positionInQueue;

    @SerializedName("result")
    private JobResult result;

    public String getJobId() {
        return jobId;
    }

    public String getStatus() {
        return status;
    }

    public int getPositionInQueue() {
        return positionInQueue;
    }

    public JobResult getResult() {
        return result;
    }

    public static class JobResult {
        @SerializedName("text")
        private String text;  // <- string, not map

        @SerializedName("session_id")
        private String sessionId;

        public String getText() {
            return text;
        }

        public String getSessionId() {
            return sessionId;
        }
    }
}

