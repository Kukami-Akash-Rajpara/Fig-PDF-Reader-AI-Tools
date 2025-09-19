package com.app.figpdfconvertor.figpdf.utils;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;

public class ImageViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public RelativeLayout overlayIcon;
    public ImageView expandButton;
    public TextView imageCount;

    public ImageView removeButton; // added for bottom sheet

    public ImageViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.image_item);
        overlayIcon = itemView.findViewById(R.id.overlay_icon);
        imageCount = itemView.findViewById(R.id.image_count);
        removeButton = itemView.findViewById(R.id.remove_button);
        expandButton = itemView.findViewById(R.id.expand_button);

    }
}