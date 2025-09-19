package com.app.figpdfconvertor.figpdf.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.utils.ImageViewHolder;

import java.util.ArrayList;
import java.util.List;

public class BottomSheetImagesAdapter extends RecyclerView.Adapter<ImageViewHolder> {

    private final List<Uri> selectedImages = new ArrayList<>();
    private final OnImageRemovedListener listener;

    public interface OnImageRemovedListener {
        void onImageRemoved(Uri removedUri);
    }

    public BottomSheetImagesAdapter(OnImageRemovedListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottom_sheet_images, parent, false);
        return new ImageViewHolder(view);
    }
    public List<Uri> getImages() {
        return new ArrayList<>(selectedImages); // assuming selectedImages is your internal list
    }
    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Uri uri = selectedImages.get(position);

        if (holder.imageView != null && uri != null) {
            Glide.with(holder.imageView.getContext())
                    .load(uri)
                    .centerCrop()
                    .into(holder.imageView);
        }

        // Hide overlay and count
        if (holder.overlayIcon != null) holder.overlayIcon.setVisibility(View.GONE);
        if (holder.imageCount != null) holder.imageCount.setVisibility(View.GONE);

        // Remove button
        if (holder.removeButton != null) {
            holder.removeButton.setVisibility(View.VISIBLE);
            holder.removeButton.setOnClickListener(v -> {
                int pos = holder.getAdapterPosition(); // new method
                if (pos != RecyclerView.NO_POSITION && listener != null) {
                    Uri removed = selectedImages.get(pos); // do NOT remove internally
                    listener.onImageRemoved(removed);      // Activity handles removal
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return selectedImages.size();
    }

    // Update the bottom sheet list from Activity
    public void updateImages(List<Uri> newImages) {
        selectedImages.clear();
        if (newImages != null) selectedImages.addAll(newImages);
        notifyDataSetChanged();
    }
}