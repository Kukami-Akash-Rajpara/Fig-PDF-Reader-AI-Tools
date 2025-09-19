package com.app.figpdfconvertor.figpdf.model.PdfSummarizer;

public class QAItem {
    private String question;
    private String answer;
    private boolean isUser;

    public QAItem(String question, String answer, boolean isUser) {
        this.question = question;
        this.answer = answer;
        this.isUser = isUser;
    }

    public String getQuestion() { return question; }
    public String getAnswer() { return answer; }
    public boolean isUser() { return isUser; }
}

