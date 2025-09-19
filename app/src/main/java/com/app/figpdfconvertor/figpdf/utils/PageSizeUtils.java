package com.app.figpdfconvertor.figpdf.utils;

import android.content.Context;

public class PageSizeUtils {

    private final Context mContext;
    public static String mPageSize = "A4"; // current selection
    private final String mDefaultPageSize;

    public PageSizeUtils(Context context) {
        this.mContext = context;
        mDefaultPageSize = "A4"; // default page size
        mPageSize = mDefaultPageSize;
    }

    // Set the page size selected from the BottomSheet
    public void setPageSize(String pageSize) {
        mPageSize = pageSize;
    }

    // Get the current page size for PDF generation
    public String getPageSize() {
        return mPageSize;
    }
}
