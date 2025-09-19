package com.app.figpdfconvertor.figpdf.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.text.HtmlCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.app.figpdfconvertor.figpdf.R;
import com.app.figpdfconvertor.figpdf.model.PdfSummarizer.QAItem;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 1;
    private static final int TYPE_AI = 2;

    private final List<QAItem> chatList;

    public ChatAdapter(List<QAItem> chatList) {
        this.chatList = chatList;
    }

    @Override
    public int getItemViewType(int position) {
        QAItem item = chatList.get(position);
        return item.isUser() ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        QAItem item = chatList.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).txtMessage.setText(HtmlCompat.fromHtml(item.getQuestion(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        } else if (holder instanceof AiViewHolder) {
            ((AiViewHolder) holder).txtMessage.setText(HtmlCompat.fromHtml(item.getAnswer(), HtmlCompat.FROM_HTML_MODE_LEGACY));
        }
    }

    @Override
    public int getItemCount() {
        if (chatList == null)
            return 0;
        return chatList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;

        UserViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtUserMsg);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView txtMessage;

        AiViewHolder(View itemView) {
            super(itemView);
            txtMessage = itemView.findViewById(R.id.txtAiMsg);
        }
    }
}
