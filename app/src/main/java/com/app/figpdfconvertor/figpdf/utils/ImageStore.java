package com.app.figpdfconvertor.figpdf.utils;

import android.net.Uri;

import java.util.ArrayList;
import java.util.List;

public class ImageStore {

    private static final List<Uri> images = new ArrayList<>();

    // Add one image (camera captured)
    public static void addImage(Uri uri) {
        if (!images.contains(uri)) images.add(uri);
    }

    // Add multiple images (adapter selections)
    public static void addImages(List<Uri> uris) {
        for (Uri uri : uris) {
            if (!images.contains(uri)) images.add(uri);
        }
    }

    // Remove one image
    public static void removeImage(Uri uri) {
        images.remove(uri);
    }

    // Clear all images
    public static void clearAll() {
        images.clear();
    }

    // Get all images
    public static List<Uri> getImages() {
        return new ArrayList<>(images);
    }

    public static void setImages(List<Uri> uris) {
        images.clear();
        images.addAll(uris);
    }
}
