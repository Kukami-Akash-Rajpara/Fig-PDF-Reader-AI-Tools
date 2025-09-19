package com.app.figpdfconvertor.figpdf.utils;

import java.util.ArrayList;

public class ImageToPDFOptions extends PDFOptions {

    private String mQualityString;
    private ArrayList<String> mImagesUri;
    private int mMarginTop = 0;
    private int mMarginBottom = 0;
    private int mMarginRight = 0;
    private int mMarginLeft = 0;
    private String mImageScaleType;
    public String mPageNumStyle;
    private String mMasterPwd;
    public boolean mWhiteMargin = false; // default off
    public String mCompression = "LOW";
    public ImageToPDFOptions() {
        super();
        setPasswordProtected(false);
        setWatermarkAdded(false);
        setBorderWidth(0);
    }

    public ImageToPDFOptions(String mFileName, String mPageSize, boolean mPasswordProtected,
                             String mPassword, String mQualityString, int mBorderWidth,
                             String masterPwd, ArrayList<String> mImagesUri,
                             boolean mWatermarkAdded, Watermark mWatermark, int pageColor) {
        super(mFileName, mPageSize, mPasswordProtected, mPassword, mBorderWidth, mWatermarkAdded, mWatermark,
                pageColor);
        this.mQualityString = mQualityString;
        this.mImagesUri = mImagesUri;
        this.mMasterPwd = masterPwd;
    }
    public boolean isWhiteMargin() {
        return mWhiteMargin;
    }
    public enum PdfOrientation {
        AUTO,
        PORTRAIT,
        LANDSCAPE
    }

    private PdfOrientation pdfOrientation = PdfOrientation.AUTO;

    // Getter
    public PdfOrientation getPdfOrientation() {
        return pdfOrientation;
    }

    // Setter
    public void setPdfOrientation(PdfOrientation pdfOrientation) {
        this.pdfOrientation = pdfOrientation;
    }

    public int getCompressionQuality() {
        if (mCompression == null) return 100;

        switch (mCompression) {
            case "LOW": return 100;       // max size, original quality
            case "REGULAR": return 75;    // large size, high quality
            case "MEDIUM": return 50;     // medium size, medium quality
            case "MAX": return 25;        // small size, low quality
            default: return 100;
        }
    }

    public float getCompressionScale() {
        if (mCompression == null) return 1.0f;

        switch (mCompression) {
            case "LOW": return 1.0f;       // max size
            case "REGULAR": return 0.75f;  // large
            case "MEDIUM": return 0.5f;    // medium
            case "MAX": return 0.25f;      // small
            default: return 1.0f;
        }
    }

    public String getCompression() {
        return mCompression;
    }

    public void setCompression(String compression) {
        this.mCompression = compression;
    }

    // Toggle method
    public void toggleWhiteMargin() {
        mWhiteMargin = !mWhiteMargin;

        if (mWhiteMargin) {
            // Set default white margin (example: 20 on each side)
            setMargins(20, 20, 20, 20);
        } else {
            // Remove margins
            setMargins(0, 0, 0, 0);
        }
    }
    public String getQualityString() {
        return mQualityString;
    }

    public ArrayList<String> getImagesUri() {
        return mImagesUri;
    }

    public void setQualityString(String mQualityString) {
        this.mQualityString = mQualityString;
    }

    public void setImagesUri(ArrayList<String> mImagesUri) {
        this.mImagesUri = mImagesUri;
    }

    public void setMargins(int top, int bottom, int right, int left) {
        mMarginTop = top;
        mMarginBottom = bottom;
        mMarginRight = right;
        mMarginLeft = left;
    }

    public void setMasterPwd(String pwd) {
        this.mMasterPwd = pwd;
    }

    public int getMarginTop() {
        return mMarginTop;
    }

    public int getMarginBottom() {
        return mMarginBottom;
    }

    public int getMarginRight() {
        return mMarginRight;
    }

    public int getMarginLeft() {
        return mMarginLeft;
    }

    public String getImageScaleType() {
        return mImageScaleType;
    }

    public void setImageScaleType(String mImageScaleType) {
        this.mImageScaleType = mImageScaleType;
    }
    public String getPageNumStyle() {
        return mPageNumStyle;
    }

    public void setPageNumStyle(String mPageNumStyle) {
        this.mPageNumStyle = mPageNumStyle;
    }

    public String getMasterPwd() {
        return mMasterPwd;
    }
}
