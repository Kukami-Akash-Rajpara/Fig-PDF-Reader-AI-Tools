package com.app.figpdfconvertor.figpdf.utils;

public interface OnPDFCreatedInterface {
    void onPDFCreationStarted();
    void onPDFCreated(boolean success, String path);
    void onPDFProgress(int progress);

}
