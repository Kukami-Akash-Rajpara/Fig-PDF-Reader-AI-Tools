package com.app.figpdfconvertor.figpdf.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.databinding.ItemCameraBinding;
import com.app.figpdfconvertor.figpdf.databinding.ItemImageBinding;

import java.util.ArrayList;
import java.util.List;

public class ImageSelectionAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_CAMERA = 0;
    private static final int VIEW_TYPE_IMAGE = 1;

    private final List<Uri> imageList;
    private final List<Integer> selectedPositions = new ArrayList<>();
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onCameraClick();
        void onImageClick(int position);
        void onExpandClick(int position);
        void onSelectionChanged(boolean hasSelection);
    }

    public ImageSelectionAdapter(List<Uri> imageList, OnItemClickListener listener) {
        this.imageList = imageList;
        this.listener = listener;
    }

    // Add more images
    public void addImages(List<Uri> newImages) {
        if (newImages == null || newImages.isEmpty()) return;
        int startIndex = imageList.size();
        imageList.addAll(newImages);
        notifyItemRangeInserted(startIndex + 1, newImages.size()); // +1 for camera tile
    }

    // Clear images & selection
    public void clearImages() {
        imageList.clear();
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedPositions.clear();
        notifyDataSetChanged();
    }

    public void selectAllImages() {
        selectedPositions.clear();
        for (int i = 0; i < imageList.size(); i++) selectedPositions.add(i);
        notifyDataSetChanged();
    }

    public void setSelectedPositions(List<Integer> positions) {
        selectedPositions.clear();
        if (positions != null) selectedPositions.addAll(positions);
        notifyDataSetChanged();
    }

    public List<Integer> getSelectedPositions() {
        return new ArrayList<>(selectedPositions);
    }

    public int getSelectedCount() {
        return selectedPositions.size();
    }

    public List<Uri> getAllImages() {
        return imageList;
    }

    public Uri getImageUri(int index) {
        if (index >= 0 && index < imageList.size()) return imageList.get(index);
        return null;
    }

    public void removeSelection(Uri uri) {
        int index = imageList.indexOf(uri);
        if (index != -1) {
            selectedPositions.remove((Integer) index);
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        // Always show at least 1 (the camera tile)
        return imageList.isEmpty() ? 1 : imageList.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        // If empty → only camera
        if (imageList.isEmpty()) return VIEW_TYPE_CAMERA;
        return position == 0 ? VIEW_TYPE_CAMERA : VIEW_TYPE_IMAGE;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_CAMERA) {
            ItemCameraBinding binding = ItemCameraBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new CameraViewHolder(binding);
        } else {
            ItemImageBinding binding = ItemImageBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ImageViewHolder(binding);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof CameraViewHolder) {
            ((CameraViewHolder) holder).bind();
        } else if (holder instanceof ImageViewHolder) {
            Uri uri = imageList.get(position - 1);
            ((ImageViewHolder) holder).bind(uri, position - 1);
        }
    }

    // 📷 Camera Tile
    class CameraViewHolder extends RecyclerView.ViewHolder {
        private final ItemCameraBinding binding;
        CameraViewHolder(ItemCameraBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind() {
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) listener.onCameraClick();
            });
        }
    }

    // 🖼️ Image Tile
    class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ItemImageBinding binding;
        ImageViewHolder(ItemImageBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
        void bind(Uri uri, int index) {
            // Load image
            Glide.with(binding.imageItem.getContext())
                    .load(uri)
                    .centerCrop()
                    .placeholder(R.drawable.tab_rounded_background)
                    .into(binding.imageItem);

            // Update selection UI
            if (selectedPositions.contains(index)) {
                binding.overlayIcon.setVisibility(View.VISIBLE);
                binding.expandButton.setVisibility(View.GONE);
                binding.imageCount.setVisibility(View.VISIBLE);
                binding.imageCount.setText(String.valueOf(selectedPositions.indexOf(index) + 1));
            } else {
                binding.overlayIcon.setVisibility(View.GONE);
                binding.expandButton.setVisibility(View.VISIBLE);
                binding.imageCount.setVisibility(View.GONE);
            }

            // Expand click
            binding.expandButton.setOnClickListener(v -> {
                if (listener != null) listener.onExpandClick(index);
            });

            // Image click (toggle select)
            binding.getRoot().setOnClickListener(v -> {
                if (selectedPositions.contains(index)) {
                    // Deselect
                    int removedRank = getSelectionRank(index);
                    selectedPositions.remove((Integer) index);

                    // Update this item
                    notifyItemChanged(getAdapterPosition());

                    // Update all items that were after this one in selection order
                    for (int i = removedRank - 1; i < selectedPositions.size(); i++) {
                        int posInAdapter = selectedPositions.get(i) + 1; // +1 for camera
                        notifyItemChanged(posInAdapter);
                    }
                } else {
                    // Select
                    selectedPositions.add(index);
                    notifyItemChanged(getAdapterPosition());
                }

                if (listener != null) {
                    listener.onImageClick(index);
                    listener.onSelectionChanged(!selectedPositions.isEmpty());
                }
            });

        }
    }
    private int getSelectionRank(int index) {
        for (int i = 0; i < selectedPositions.size(); i++) {
            if (selectedPositions.get(i) == index) {
                return i + 1; // rank is 1-based
            }
        }
        return -1;
    }
}
