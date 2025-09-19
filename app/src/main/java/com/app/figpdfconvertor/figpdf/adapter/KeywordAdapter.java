package com.app.figpdfconvertor.figpdf.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;

import java.util.List;

public class KeywordAdapter extends RecyclerView.Adapter<KeywordAdapter.KeywordViewHolder> {

    private final List<String> keywordList;
    private final boolean isFoundList; // true = green, false = red

    public KeywordAdapter(List<String> keywordList, boolean isFoundList) {
        this.keywordList = keywordList;
        this.isFoundList = isFoundList;
    }

    @NonNull
    @Override
    public KeywordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_keyword, parent, false);
        return new KeywordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull KeywordViewHolder holder, int position) {
        String keyword = keywordList.get(position);
        holder.txtKeyword.setText(keyword);

        if (isFoundList) {
            // ✅ Found keywords = Green
            holder.layKeyword.setBackgroundResource(R.drawable.bg_resume_keyword_green);
            holder.txtKeyword.setTextColor(Color.parseColor("#2A8401"));
        } else {
            // ❌ Missing keywords = Red
            holder.layKeyword.setBackgroundResource(R.drawable.bg_resume_keyword_red);
            holder.txtKeyword.setTextColor(Color.parseColor("#B10404"));
        }
    }

    @Override
    public int getItemCount() {
        return keywordList != null ? keywordList.size() : 0;
    }

    static class KeywordViewHolder extends RecyclerView.ViewHolder {
        LinearLayout layKeyword;
        TextView txtKeyword;

        public KeywordViewHolder(@NonNull View itemView) {
            super(itemView);
            layKeyword = itemView.findViewById(R.id.layKeyword);
            txtKeyword = itemView.findViewById(R.id.txtKeyword);
        }
    }
}
