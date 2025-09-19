package com.app.figpdfconvertor.figpdf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;

import java.util.List;

public class SuggestedQuestionAdapter extends RecyclerView.Adapter<SuggestedQuestionAdapter.ViewHolder> {

    public interface OnQuestionClickListener {
        void onQuestionClick(String question);
    }

    private final List<String> questionList;
    private final OnQuestionClickListener listener;

    public SuggestedQuestionAdapter(List<String> questionList, OnQuestionClickListener listener) {
        this.questionList = questionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_suggested_question, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String question = questionList.get(position);
        holder.txtQuestion.setText(question);

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onQuestionClick(question);
            }
        });
    }

    @Override
    public int getItemCount() {
        return questionList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtQuestion;

        ViewHolder(View itemView) {
            super(itemView);
            txtQuestion = itemView.findViewById(R.id.txtQuestion);
        }
    }
}

