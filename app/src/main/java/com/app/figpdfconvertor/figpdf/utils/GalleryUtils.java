package com.app.figpdfconvertor.figpdf.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.util.ArrayList;
import java.util.List;

public class GalleryUtils {
//    public static List<Uri> loadAllImages(Context context) {
//        List<Uri> imageUris = new ArrayList<>();
//        Uri collection = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        String[] projection = { MediaStore.Images.Media._ID };
//
//        try (Cursor cursor = context.getContentResolver().query(
//                collection, projection, null, null, MediaStore.Images.Media.DATE_ADDED + " DESC")) {
//
//            if (cursor != null) {
//                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
//                while (cursor.moveToNext()) {
//                    long id = cursor.getLong(idColumn);
//                    Uri uri = Uri.withAppendedPath(collection, String.valueOf(id));
//                    imageUris.add(uri);
//                }
//            }
//        }
//        return imageUris;
//    }
}