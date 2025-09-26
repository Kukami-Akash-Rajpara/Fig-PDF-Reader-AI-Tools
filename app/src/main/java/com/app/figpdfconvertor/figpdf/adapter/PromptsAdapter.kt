package com.app.figpdfconvertor.figpdf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R

class PromptsAdapter(
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<PromptsAdapter.PromptViewHolder>() {

    private val prompts = mutableListOf<String>()

    fun submitList(newList: List<String>) {
        prompts.clear()
        prompts.addAll(newList)
        notifyDataSetChanged()
    }

    inner class PromptViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.promptText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PromptViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_prompt, parent, false)
        return PromptViewHolder(view)
    }

    override fun onBindViewHolder(holder: PromptViewHolder, position: Int) {
        holder.textView.text = prompts[position]
        holder.itemView.setOnClickListener {
            onItemClick(prompts[position]) // click callback
        }
    }

    override fun getItemCount(): Int = prompts.size
}
