package com.app.figpdfconvertor.figpdf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;

public class ColorAdapter extends RecyclerView.Adapter<ColorAdapter.ColorViewHolder> {

    private final int[] colors;
    private int selectedPosition = -1;
    private final OnColorClickListener listener;

    public interface OnColorClickListener {
        void onColorClick(int color);
    }

    public ColorAdapter(int[] colors, OnColorClickListener listener) {
        this.colors = colors;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ColorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_color_circle, parent, false);
        return new ColorViewHolder(view);
    }


    @Override
    public void onBindViewHolder(@NonNull ColorViewHolder holder, int position) {
    int color = colors[position];

    if (selectedPosition == position) {
        // Use selected background
        holder.circle.setBackgroundResource(R.drawable.color_circle_selected);
    } else {
        // Use default background
        holder.circle.setBackgroundResource(R.drawable.color_circle_bg);
    }

    // Apply actual color inside
    holder.circle.getBackground().setTint(color);

        holder.itemView.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return; // safety check

            int prev = selectedPosition;
            selectedPosition = pos;

            if (prev >= 0) notifyItemChanged(prev);
            notifyItemChanged(pos);

            listener.onColorClick(colors[pos]);
        });
}

    @Override
    public int getItemCount() {
        return colors.length;
    }

    static class ColorViewHolder extends RecyclerView.ViewHolder {
        View circle;
        public ColorViewHolder(View itemView) {
            super(itemView);
            circle = itemView.findViewById(R.id.colorCircle);
        }
    }
}