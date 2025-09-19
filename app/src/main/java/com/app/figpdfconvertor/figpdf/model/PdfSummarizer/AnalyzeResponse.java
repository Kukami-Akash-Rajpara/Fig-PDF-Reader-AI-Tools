package com.app.figpdfconvertor.figpdf.model.PdfSummarizer;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class AnalyzeResponse{

	@SerializedName("summary")
	private String summary;

	@SerializedName("original_language")
	private String originalLanguage;

	@SerializedName("target_language")
	private String targetLanguage;

	@SerializedName("cached")
	private boolean cached;

	@SerializedName("questions")
	private List<String> questions;

	@SerializedName("session_id")
	private String sessionId;

	@SerializedName("summary_key")
	private String summaryKey;

	public String getSummary(){
		return summary;
	}

	public String getOriginalLanguage(){
		return originalLanguage;
	}

	public String getTargetLanguage(){
		return targetLanguage;
	}

	public boolean isCached(){
		return cached;
	}

	public List<String> getQuestions(){
		return questions;
	}

	public String getSessionId(){
		return sessionId;
	}

	public String getSummaryKey(){
		return summaryKey;
	}
}