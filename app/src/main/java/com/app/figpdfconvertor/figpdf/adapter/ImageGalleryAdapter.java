package com.app.figpdfconvertor.figpdf.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;

import java.util.ArrayList;
import java.util.List;

// Adapter class for displaying a list of images in a RecyclerView
public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.ImageViewHolder> {

    // List of image URIs (the data source)
    private final List<Uri> imageUris;
    private final List<Integer> selectedPositions = new ArrayList<>();


    public interface OnSelectionChangeListener {
        void onSelectionChanged(int selectedCount);
    }

    public List<Integer> getSelectedPositions() {
        return selectedPositions;
    }

    public List<Uri> getImageUris() {
        return imageUris;
    }

    // Constructor: takes the list of image URIs when creating the adapter
    public ImageGalleryAdapter(List<Uri> imageUris) {
        this.imageUris = imageUris;
//        this.listener = listener;
    }

    private OnSelectionChangeListener selectionChangeListener;

    public void setOnSelectionChangeListener(OnSelectionChangeListener listener) {
        this.selectionChangeListener = listener;
    }

    // Called when RecyclerView needs to create a new ViewHolder
    // Inflates the layout (item_fullscreen_image.xml) for each item
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate = turn the XML layout into a real View object
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fullscreen_image, parent, false);

        // Wrap that view inside a ViewHolder
        return new ImageViewHolder(view);
    }

    // Called when RecyclerView wants to display data at a given position
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = imageUris.get(position);
        holder.imageView.setImageURI(uri);


    }

    // Tells RecyclerView how many items it needs to render
    @Override
    public int getItemCount() {
        // If imageUris is null, return 0, else return its size
        return imageUris != null ? imageUris.size() : 0;
    }

    // Inner class that holds references to the views for each item
    // Helps RecyclerView efficiently reuse views instead of creating new ones each time
    public class ImageViewHolder extends RecyclerView.ViewHolder {
        public ImageView imageView;
        public AppCompatCheckBox customCheckbox;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.fullscreen_image);
        }
    }
}