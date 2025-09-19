package com.app.figpdfconvertor.figpdf.utils;

import static android.os.ParcelFileDescriptor.MODE_READ_ONLY;
import static android.widget.Toast.LENGTH_SHORT;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.pdf.PdfRenderer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.app.figpdfconvertor.figpdf.interfaces.OnPDFCompressedInterface;
import com.app.figpdfconvertor.figpdf.interfaces.OnPdfReorderedInterface;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PRStream;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfImportedPage;
import com.itextpdf.text.pdf.PdfName;
import com.itextpdf.text.pdf.PdfNumber;
import com.itextpdf.text.pdf.PdfObject;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.PdfStamper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.parser.PdfImageObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class PDFUtils {

    private final Activity mContext;
    //private final FileUtils mFileUtils;

    public PDFUtils(Activity context) {
        this.mContext = context;
       // this.mFileUtils = new FileUtils(mContext);
    }

    /**
     * Creates a mDialog with details of given PDF file
     *
     * @param file - file name
     */

    /**
     * Check if a PDF at given path is encrypted
     *
     * @param path - path of PDF
     * @return true - if encrypted otherwise false
     */
    @WorkerThread
    public boolean isPDFEncrypted(String path) {
        boolean isEncrypted;
        PdfReader pdfReader = null;
        try {
            pdfReader = new PdfReader(path);
            isEncrypted = pdfReader.isEncrypted();
        } catch (IOException e) {
            isEncrypted = true;
        } finally {
            if (pdfReader != null) pdfReader.close();
        }
        return isEncrypted;
    }

    public void compressPDF(String inputPath, String outputPath, int quality,
                            OnPDFCompressedInterface onPDFCompressedInterface) {
        new CompressPdfAsync(inputPath, outputPath, quality, onPDFCompressedInterface)
                .execute();
    }

    private static class CompressPdfAsync extends AsyncTask<String, String, String> {

        final int quality;
        final String inputPath;
        final String outputPath;
        boolean success;
        final OnPDFCompressedInterface mPDFCompressedInterface;

        CompressPdfAsync(String inputPath, String outputPath, int quality,
                         OnPDFCompressedInterface onPDFCompressedInterface) {
            this.inputPath = inputPath;
            this.outputPath = outputPath;
            this.quality = quality;
            this.mPDFCompressedInterface = onPDFCompressedInterface;
            success = false;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mPDFCompressedInterface.pdfCompressionStarted();
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                PdfReader reader = new PdfReader(inputPath);
                compressReader(reader);
                saveReader(reader);
                reader.close();
                success = true;
            } catch (IOException | DocumentException e) {
                e.printStackTrace();
                success = false;
            }
            return null;
        }

        /**
         * Attempt to compress each object in a PdfReader
         * @param reader - PdfReader to have objects compressed
         * @throws IOException
         */
        private void compressReader(PdfReader reader) throws IOException {
            int n = reader.getXrefSize();
            PdfObject object;
            PRStream stream;

            for (int i = 0; i < n; i++) {
                object = reader.getPdfObject(i);
                if (object == null || !object.isStream())
                    continue;
                stream = (PRStream) object;
                compressStream(stream);
            }

            reader.removeUnusedObjects();
        }

        /**
         * If given stream is image compress it
         * @param stream - Steam to be compressed
         * @throws IOException
         */
        private void compressStream(PRStream stream) throws IOException {
            PdfObject pdfSubType = stream.get(PdfName.SUBTYPE);
            System.out.println(stream.type());
            if (pdfSubType != null && pdfSubType.toString().equals(PdfName.IMAGE.toString())) {
                PdfImageObject image = new PdfImageObject(stream);
                byte[] imageBytes = image.getImageAsBytes();
                Bitmap bmp;
                bmp = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                if (bmp == null) return;

                int width = bmp.getWidth();
                int height = bmp.getHeight();

                Bitmap outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                Canvas outCanvas = new Canvas(outBitmap);
                outCanvas.drawBitmap(bmp, 0f, 0f, null);

                ByteArrayOutputStream imgBytes = new ByteArrayOutputStream();
                outBitmap.compress(Bitmap.CompressFormat.JPEG, quality, imgBytes);
                stream.clear();
                stream.setData(imgBytes.toByteArray(), false, PRStream.BEST_COMPRESSION);
                stream.put(PdfName.TYPE, PdfName.XOBJECT);
                stream.put(PdfName.SUBTYPE, PdfName.IMAGE);
                stream.put(PdfName.FILTER, PdfName.DCTDECODE);
                stream.put(PdfName.WIDTH, new PdfNumber(width));
                stream.put(PdfName.HEIGHT, new PdfNumber(height));
                stream.put(PdfName.BITSPERCOMPONENT, new PdfNumber(8));
                stream.put(PdfName.COLORSPACE, PdfName.DEVICERGB);
            }
        }

        /**
         * Save changes to given reader's data to the output path
         * @param reader - changed reader
         * @throws DocumentException
         * @throws IOException
         */
        private void saveReader(PdfReader reader) throws DocumentException, IOException {
            PdfStamper stamper = new PdfStamper(reader, new FileOutputStream(outputPath));
            stamper.setFullCompression();
            stamper.close();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mPDFCompressedInterface.pdfCompressionEnded(outputPath, success);
        }
    }

    /*public void createPdfFromImages(String outputPath, ArrayList<String> images) {
        PdfDocument pdfDocument = new PdfDocument();

        for (int i = 0; i < images.size(); i++) {
            String imagePath = images.get(i);
            Bitmap bitmap = BitmapFactory.decodeFile(imagePath);

            if (bitmap != null) {
                // ✅ Scale to A4 instead of raw image px
                int pageWidth = 595;   // A4 in points (approx 8.3in × 72dpi)
                int pageHeight = 842;  // A4 in points (approx 11.7in × 72dpi)

                PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(
                        pageWidth,
                        pageHeight,
                        i + 1
                ).create();

                PdfDocument.Page page = pdfDocument.startPage(pageInfo);

                // Scale bitmap to fit page
                float scale = Math.min(
                        (float) pageWidth / bitmap.getWidth(),
                        (float) pageHeight / bitmap.getHeight()
                );

                int newWidth = (int) (bitmap.getWidth() * scale);
                int newHeight = (int) (bitmap.getHeight() * scale);

                Bitmap scaled = Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);

                float left = (pageWidth - newWidth) / 2f;
                float top = (pageHeight - newHeight) / 2f;

                page.getCanvas().drawBitmap(scaled, left, top, null);

                pdfDocument.finishPage(page);

                bitmap.recycle();
                scaled.recycle();
            }
        }

        try {
            File file = new File(outputPath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            FileOutputStream fos = new FileOutputStream(file);
            pdfDocument.writeTo(fos);
            fos.flush();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            pdfDocument.close();
        }
    }*/

    public boolean createPdfFromImages(String outputPath, ArrayList<String> images) {
        Document document = new Document();

        try {
            File file = new File(outputPath);
            if (file.getParentFile() != null) {
                file.getParentFile().mkdirs();
            }

            PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            for (String imagePath : images) {
                Image image = Image.getInstance(imagePath);

                // Scale image to fit page size
                image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());

                // Center image
                image.setAlignment(Image.ALIGN_CENTER | Image.ALIGN_MIDDLE);

                // Add image
                document.add(image);

                // New page for next image
                document.newPage();
            }

            document.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (document.isOpen()) {
                document.close();
            }
            return false;
        }
    }



    /**
     * Main function to add images to PDF
     *
     * @param inputPath - path of input PDF
     * @param output    - path of output PDF
     * @param imagesUri - list of images to add
     * @return true, if succeeded, otherwise false
     */
    public boolean addImagesToPdf(String inputPath, String output, ArrayList<String> imagesUri) {
        try {
            PdfReader reader = new PdfReader(inputPath);
            Document document = new Document();
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(output));
            document.open();
            initDoc(reader, document, writer);
            appendImages(document, imagesUri);
            document.close();

            Toast.makeText(mContext,"Created",LENGTH_SHORT).show();
        /*    StringUtils.getInstance().getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, v ->
                            mFileUtils.openFile(output, FileUtils.FileType.e_PDF)).show();*/
//            new DatabaseHelper(mContext).insertRecord(output, mContext.getString(R.string.created));

            return true;
        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            Log.e("EXCEPTIONNN",e.getMessage());
         //   StringUtils.getInstance().showSnackbar(mContext, R.string.remove_pages_error);
            return false;
        }
    }

    /**
     * Initialise document with pages from reader to writer
     * @param reader -
     * @param document
     * @param writer
     */
    private void initDoc(PdfReader reader, Document document, PdfWriter writer) {
        int numOfPages = reader.getNumberOfPages();
        PdfContentByte cb = writer.getDirectContent();
        PdfImportedPage importedPage;
        for (int page = 1; page <= numOfPages; page++) {
            importedPage = writer.getImportedPage(reader, page);
            document.newPage();
            cb.addTemplate(importedPage, 0, 0);
        }
    }

    /**
     * Add images at given URIs to end of given document
     * @param document
     * @param imagesUri
     * @throws DocumentException
     * @throws IOException
     */
    private void appendImages(Document document, ArrayList<String> imagesUri) throws DocumentException, IOException {
        Rectangle documentRect = document.getPageSize();
        for (int i = 0; i < imagesUri.size(); i++) {
            document.newPage();
            Image image = Image.getInstance(imagesUri.get(i));
            image.setBorder(0);
            float pageWidth = document.getPageSize().getWidth(); // - (mMarginLeft + mMarginRight);
            float pageHeight = document.getPageSize().getHeight(); // - (mMarginBottom + mMarginTop);
            image.scaleToFit(pageWidth, pageHeight);
            image.setAbsolutePosition(
                    (documentRect.getWidth() - image.getScaledWidth()) / 2,
                    (documentRect.getHeight() - image.getScaledHeight()) / 2);
            document.add(image);
        }
    }

   /* public boolean reorderRemovePDF(String inputPath, String output, String pages) {
        try {
            PdfReader reader = new PdfReader(inputPath);
            reader.selectPages(pages);
            if (reader.getNumberOfPages() == 0) {
                StringUtils.getInstance().showSnackbar(mContext, R.string.remove_pages_error);
                return false;
            }
            //if (reader.getNumberOfPages() )
            PdfStamper pdfStamper = new PdfStamper(reader,
                    new FileOutputStream(output));
            pdfStamper.close();
            StringUtils.getInstance().getSnackbarwithAction(mContext, R.string.snackbar_pdfCreated)
                    .setAction(R.string.snackbar_viewAction, v ->
                            mFileUtils.openFile(output, FileUtils.FileType.e_PDF)).show();
//            new DatabaseHelper(mContext).insertRecord(output,
//                    mContext.getString(R.string.created));
            return true;

        } catch (IOException | DocumentException e) {
            e.printStackTrace();
            StringUtils.getInstance().showSnackbar(mContext, R.string.remove_pages_error);
            return false;
        }
    }*/

    /**
     * @param uri Uri of the pdf
     * @param path Absolute path of the pdf
     * @param onPdfReorderedInterface interface to update  pdf reorder progress
     * */
    public void reorderPdfPages(Uri uri, String path, @NonNull OnPdfReorderedInterface onPdfReorderedInterface) {
        new ReorderPdfPagesAsync(uri, path, mContext, onPdfReorderedInterface).execute();
    }

    private class ReorderPdfPagesAsync extends AsyncTask<String, String, ArrayList<Bitmap>> {

        private final Uri mUri;
        private final String mPath;
        private final OnPdfReorderedInterface mOnPdfReorderedInterface;
        private final Activity mActivity;

        /**
         * @param uri Uri of the pdf
         * @param path Absolute path of the pdf
         * @param onPdfReorderedInterface interface to update  pdf reorder progress
         * @param activity Its needed to get the current context
         * */

        ReorderPdfPagesAsync(Uri uri,
                             String path,
                             Activity activity,
                             OnPdfReorderedInterface onPdfReorderedInterface) {
            this.mUri = uri;
            this.mPath = path;
            this.mOnPdfReorderedInterface = onPdfReorderedInterface;
            this.mActivity = activity;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mOnPdfReorderedInterface.onPdfReorderStarted();
        }

        @Override
        protected ArrayList<Bitmap> doInBackground(String... strings) {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            ParcelFileDescriptor fileDescriptor = null;
            try {
                if (mUri != null)
                    fileDescriptor = mActivity.getContentResolver().openFileDescriptor(mUri, "r");
                else if (mPath != null)
                    fileDescriptor = ParcelFileDescriptor.open(new File(mPath), MODE_READ_ONLY);
                if (fileDescriptor != null) {
                    PdfRenderer renderer = new PdfRenderer(fileDescriptor);
                    bitmaps = getBitmaps(renderer);
                    // close the renderer
                    renderer.close();
                }
            } catch (IOException | SecurityException | IllegalArgumentException | OutOfMemoryError e) {
                e.printStackTrace();
            }
            return bitmaps;
        }

        /**
         * Get list of Bitmaps from PdfRenderer
         * @param renderer
         * @return
         */
        private ArrayList<Bitmap> getBitmaps(PdfRenderer renderer) {
            ArrayList<Bitmap> bitmaps = new ArrayList<>();
            final int pageCount = renderer.getPageCount();
            for (int i = 0; i < pageCount; i++) {
                PdfRenderer.Page page = renderer.openPage(i);
                Bitmap bitmap = Bitmap.createBitmap(page.getWidth(), page.getHeight(),
                        Bitmap.Config.ARGB_8888);
                // say we render for showing on the screen
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

                // do stuff with the bitmap
                bitmaps.add(bitmap);
                // close the page
                page.close();
            }
            return bitmaps;
        }

        @Override
        protected void onPostExecute(ArrayList<Bitmap> bitmaps) {
            super.onPostExecute(bitmaps);
            if (bitmaps != null && !bitmaps.isEmpty()) {
                mOnPdfReorderedInterface.onPdfReorderCompleted(bitmaps);
            } else {
                mOnPdfReorderedInterface.onPdfReorderFailed();
            }
        }
    }

}
