package com.app.figpdfconvertor.figpdf.model.ResumeAnalyzer;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AtsKeywordsAnalysis implements Parcelable {

    @SerializedName("keywords_found")
    private List<String> keywordsFound;

    @SerializedName("keywords_missing")
    private List<String> keywordsMissing;

    public AtsKeywordsAnalysis(List<String> keywordsFound, List<String> keywordsMissing) {
        this.keywordsFound = keywordsFound;
        this.keywordsMissing = keywordsMissing;
    }

    public List<String> getKeywordsFound() {
        return keywordsFound;
    }

    public List<String> getKeywordsMissing() {
        return keywordsMissing;
    }

    // Parcelable implementation
    protected AtsKeywordsAnalysis(Parcel in) {
        keywordsFound = in.createStringArrayList();
        keywordsMissing = in.createStringArrayList();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringList(keywordsFound);
        dest.writeStringList(keywordsMissing);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AtsKeywordsAnalysis> CREATOR = new Creator<AtsKeywordsAnalysis>() {
        @Override
        public AtsKeywordsAnalysis createFromParcel(Parcel in) {
            return new AtsKeywordsAnalysis(in);
        }

        @Override
        public AtsKeywordsAnalysis[] newArray(int size) {
            return new AtsKeywordsAnalysis[size];
        }
    };
}