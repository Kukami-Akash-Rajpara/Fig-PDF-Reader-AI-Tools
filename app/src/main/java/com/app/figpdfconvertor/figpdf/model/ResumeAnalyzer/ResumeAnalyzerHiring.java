package com.app.figpdfconvertor.figpdf.model.ResumeAnalyzer;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

public class ResumeAnalyzerHiring implements Parcelable {

    @SerializedName("resume_feedback")
    private String resumeFeedback;

    @SerializedName("requirements_score")
    private int requirementsScore;

    @SerializedName("keywords_score")
    private int keywordsScore;

    @SerializedName("technical_skills_score")
    private int technicalSkillsScore;

    @SerializedName("ats_keywords_analysis")
    private AtsKeywordsAnalysis atsKeywordsAnalysis;

    @SerializedName("hiring_overview")
    private String hiringOverview;

    @SerializedName("session_id")
    private String sessionId;

    @SerializedName("analysis")
    private String analysis;

    @SerializedName("message")
    private String message;

    @SerializedName("overall_score")
    private int overallScore;

    public String getResumeFeedback() {
        return resumeFeedback;
    }

    public int getRequirementsScore() {
        return requirementsScore;
    }

    public int getKeywordsScore() {
        return keywordsScore;
    }

    public int getTechnicalSkillsScore() {
        return technicalSkillsScore;
    }

    public AtsKeywordsAnalysis getAtsKeywordsAnalysis() {
        return atsKeywordsAnalysis;
    }

    public String getHiringOverview() {
        return hiringOverview;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAnalysis() {
        return analysis;
    }

    public String getMessage() {
        return message;
    }

    public int getOverallScore() {
        return overallScore;
    }

    protected ResumeAnalyzerHiring(Parcel in) {
        resumeFeedback = in.readString();
        requirementsScore = in.readInt();
        keywordsScore = in.readInt();
        technicalSkillsScore = in.readInt();
        atsKeywordsAnalysis = in.readParcelable(AtsKeywordsAnalysis.class.getClassLoader());
        hiringOverview = in.readString();
        sessionId = in.readString();
        analysis = in.readString();
        message = in.readString();
        overallScore = in.readInt();
    }

    public static final Creator<ResumeAnalyzerHiring> CREATOR = new Creator<ResumeAnalyzerHiring>() {
        @Override
        public ResumeAnalyzerHiring createFromParcel(Parcel in) {
            return new ResumeAnalyzerHiring(in);
        }

        @Override
        public ResumeAnalyzerHiring[] newArray(int size) {
            return new ResumeAnalyzerHiring[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(resumeFeedback);
        dest.writeInt(requirementsScore);
        dest.writeInt(keywordsScore);
        dest.writeInt(technicalSkillsScore);
        dest.writeParcelable(atsKeywordsAnalysis, flags);
        dest.writeString(hiringOverview);
        dest.writeString(sessionId);
        dest.writeString(analysis);
        dest.writeString(message);
        dest.writeInt(overallScore);
    }

    @Override
    public int describeContents() {
        return 0;
    }
}