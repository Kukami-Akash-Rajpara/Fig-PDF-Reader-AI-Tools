package com.app.figpdfconvertor.figpdf.utils;

import static com.app.figpdfconvertor.figpdf.utils.Constants.IMAGE_SCALE_TYPE_ASPECT_RATIO;
import static com.app.figpdfconvertor.figpdf.utils.Constants.pdfExtension;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * An async task that converts selected images to Pdf
 */
public class CreatePdf extends AsyncTask<String, String, String> {

    private final String mFileName;
    private final String mPassword;
    private final String mQualityString;
    private final ArrayList<String> mImagesUri;
    private final int mBorderWidth;
    private final OnPDFCreatedInterface mOnPDFCreatedInterface;
    private boolean mSuccess;
    private String mPath;
    private final String mPageSize;
    private final boolean mPasswordProtected;
    private final Boolean mWatermarkAdded;
    private final Watermark mWatermark;
    private final int mMarginTop;
    private final int mMarginBottom;
    private final int mMarginRight;
    private final int mMarginLeft;
    private final String mImageScaleType;
    private final String mPageNumStyle;
    private final String mMasterPwd;
    private final int mPageColor;
    private final ImageToPDFOptions.PdfOrientation mPdfOrientation;

    public CreatePdf(ImageToPDFOptions mImageToPDFOptions, String parentPath,
                     OnPDFCreatedInterface onPDFCreated) {
        this.mImagesUri = mImageToPDFOptions.getImagesUri();
        this.mFileName = mImageToPDFOptions.getOutFileName();
        this.mPassword = mImageToPDFOptions.getPassword();
        this.mQualityString = mImageToPDFOptions.getQualityString();
        this.mOnPDFCreatedInterface = onPDFCreated;
        this.mPageSize = mImageToPDFOptions.getPageSize();
        this.mPasswordProtected = mImageToPDFOptions.isPasswordProtected();
        this.mBorderWidth = mImageToPDFOptions.getBorderWidth();
        this.mWatermarkAdded = mImageToPDFOptions.isWatermarkAdded();
        this.mWatermark = mImageToPDFOptions.getWatermark();
        this.mMarginTop = mImageToPDFOptions.getMarginTop();
        this.mMarginBottom = mImageToPDFOptions.getMarginBottom();
        this.mMarginRight = mImageToPDFOptions.getMarginRight();
        this.mMarginLeft = mImageToPDFOptions.getMarginLeft();
        this.mImageScaleType = mImageToPDFOptions.getImageScaleType();
        this.mPageNumStyle = mImageToPDFOptions.getPageNumStyle();
        this.mMasterPwd = mImageToPDFOptions.getMasterPwd();
        this.mPageColor = mImageToPDFOptions.getPageColor();
        this.mPdfOrientation = mImageToPDFOptions.getPdfOrientation(); // ✅ orientation
        this.mPath = parentPath;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mSuccess = true;
        mOnPDFCreatedInterface.onPDFCreationStarted();
    }

    private void setFilePath() {
        File folder;

        if (android.os.Build.VERSION.SDK_INT > android.os.Build.VERSION_CODES.Q) {
            folder = new File(mPath); // mPath is the folder path
            if (!folder.exists()) folder.mkdirs();

            // Correct: append separator
            File file = new File(folder, mFileName + pdfExtension);
            mPath = file.getAbsolutePath(); // ✅ final PDF path

       /* folder = new File(mPath);
        if (!folder.exists()) folder.mkdir();
        mPath = mPath + mFileName + pdfExtension;*/
        } else {
            folder = new File(MyApp.getInstance().getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "PDF Converter");
            if (!folder.exists()) folder.mkdirs();

            File file = new File(folder, mFileName + pdfExtension);
            mPath = file.getAbsolutePath();

            if (ContextCompat.checkSelfPermission(MyApp.getInstance(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {

                File sourceFile = file;
                if (sourceFile.exists()) {
                    File destinationFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), "PDF Converter");
                    if (!destinationFolder.exists()) destinationFolder.mkdirs();

                    File destinationFile = new File(destinationFolder, sourceFile.getName());

                    try (FileInputStream fis = new FileInputStream(sourceFile);
                         FileOutputStream fos = new FileOutputStream(destinationFile)) {

                        byte[] buffer = new byte[1024];
                        int length;
                        while ((length = fis.read(buffer)) > 0) {
                            fos.write(buffer, 0, length);
                        }
                        Log.d("FileCopy", "File successfully copied to: " + destinationFile.getAbsolutePath());
                    } catch (IOException e) {
                        Log.e("FileCopy", "Error while copying file", e);
                    }
                }
            }
        }
    }

    @Override
    protected String doInBackground(String... params) {

        setFilePath();
        Log.v("stage 1", "store the pdf in sd card");

        String pageSizeName = (mPageSize == null || mPageSize.isEmpty()) ? "A4" : mPageSize;
        Rectangle pageSizeRect = PageSize.getRectangle(pageSizeName);

        // ✅ Handle orientation
        if (mPdfOrientation != null) {
            switch (mPdfOrientation) {
                case PORTRAIT:
                    if (pageSizeRect.getWidth() > pageSizeRect.getHeight())
                        pageSizeRect = new Rectangle(pageSizeRect.getHeight(), pageSizeRect.getWidth());
                    break;
                case LANDSCAPE:
                    if (pageSizeRect.getWidth() < pageSizeRect.getHeight())
                        pageSizeRect = new Rectangle(pageSizeRect.getHeight(), pageSizeRect.getWidth());
                    break;
                case AUTO:
                    if (mImagesUri != null && !mImagesUri.isEmpty()) {
                        try {
                            Bitmap firstBitmap = BitmapFactory.decodeFile(mImagesUri.get(0));
                            if (firstBitmap != null) {
                                if (firstBitmap.getWidth() > firstBitmap.getHeight()) { // landscape
                                    if (pageSizeRect.getWidth() < pageSizeRect.getHeight())
                                        pageSizeRect = new Rectangle(pageSizeRect.getHeight(), pageSizeRect.getWidth());
                                } else { // portrait
                                    if (pageSizeRect.getWidth() > pageSizeRect.getHeight())
                                        pageSizeRect = new Rectangle(pageSizeRect.getHeight(), pageSizeRect.getWidth());
                                }
                                firstBitmap.recycle();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
        Rectangle original = PageSize.getRectangle(pageSizeName);
        Rectangle pageSize = new Rectangle(original.getWidth(), original.getHeight());
        pageSize.setBackgroundColor(getBaseColor(mPageColor));
//        pageSizeRect.setBackgroundColor(getBaseColor(mPageColor));
        Document document = new Document(pageSizeRect, mMarginLeft, mMarginRight, mMarginTop, mMarginBottom);
        Log.v("stage 2", "Document Created");

        Rectangle documentRect = document.getPageSize();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(mPath));

            if (mPasswordProtected) {
                writer.setEncryption(mPassword.getBytes(), mMasterPwd.getBytes(),
                        PdfWriter.ALLOW_PRINTING | PdfWriter.ALLOW_COPY,
                        PdfWriter.ENCRYPTION_AES_128);
            }

            if (mWatermarkAdded) {
                WatermarkPageEvent watermarkPageEvent = new WatermarkPageEvent();
                watermarkPageEvent.setWatermark(mWatermark);
                writer.setPageEvent(watermarkPageEvent);
            }

            document.open();

            for (int i = 0; i < mImagesUri.size(); i++) {
                int quality = 30;
                if (StringUtils.getInstance().isNotEmpty(mQualityString)) {
                    quality = Integer.parseInt(mQualityString);
                }

                Image image = Image.getInstance(mImagesUri.get(i));
                double qualityMod = quality * 0.09;
                image.setCompressionLevel((int) qualityMod);
                image.setBorder(Rectangle.BOX);
                image.setBorderWidth(mBorderWidth);

                Bitmap bitmap = BitmapFactory.decodeFile(mImagesUri.get(i));

                float pageWidth = document.getPageSize().getWidth() - (mMarginLeft + mMarginRight);
                float pageHeight = document.getPageSize().getHeight() - (mMarginBottom + mMarginTop);
                if (mImageScaleType.equals(IMAGE_SCALE_TYPE_ASPECT_RATIO))
                    image.scaleToFit(pageWidth, pageHeight);
                else
                    image.scaleAbsolute(pageWidth, pageHeight);

                image.setAbsolutePosition(
                        (documentRect.getWidth() - image.getScaledWidth()) / 2,
                        (documentRect.getHeight() - image.getScaledHeight()) / 2
                );

                addPageNumber(documentRect, writer);
                document.add(image);
                document.newPage();
                int progress = (i + 1) * 100 / mImagesUri.size();
                publishProgress(String.valueOf(progress));
                if (bitmap != null) bitmap.recycle();
            }

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
            mSuccess = false;
        }

        return null;
    }


    @Override
    protected void onProgressUpdate(String... values) {
        super.onProgressUpdate(values);
        if (mOnPDFCreatedInterface != null) {
            try {
                int progress = Integer.parseInt(values[0]);
                mOnPDFCreatedInterface.onPDFProgress(progress);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
    }

    private void addPageNumber(Rectangle documentRect, PdfWriter writer) {
        if (mPageNumStyle != null) {
            ColumnText.showTextAligned(writer.getDirectContent(),
                    Element.ALIGN_BOTTOM,
                    getPhrase(writer, mPageNumStyle, mImagesUri.size()),
                    ((documentRect.getRight() + documentRect.getLeft()) / 2),
                    documentRect.getBottom() + 25, 0);
        }
    }

    @NonNull
    private Phrase getPhrase(PdfWriter writer, String pageNumStyle, int size) {
        switch (pageNumStyle) {
            case Constants.PG_NUM_STYLE_PAGE_X_OF_N:
                return new Phrase(String.format("Page %d of %d", writer.getPageNumber(), size));
            case Constants.PG_NUM_STYLE_X_OF_N:
                return new Phrase(String.format("%d of %d", writer.getPageNumber(), size));
            default:
                return new Phrase(String.format("%d", writer.getPageNumber()));
        }
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        mOnPDFCreatedInterface.onPDFCreated(mSuccess, mPath);
    }

    private BaseColor getBaseColor(int color) {
        return new BaseColor(Color.red(color), Color.green(color), Color.blue(color));
    }
}
