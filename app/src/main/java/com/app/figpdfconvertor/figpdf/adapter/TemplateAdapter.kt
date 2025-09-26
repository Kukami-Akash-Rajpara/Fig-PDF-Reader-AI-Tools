package com.app.figpdfconvertor.figpdf.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.model.TemplateItem
import com.bumptech.glide.Glide

class TemplateAdapter(
    private val onItemClick: (TemplateItem) -> Unit
) : RecyclerView.Adapter<TemplateAdapter.TemplateViewHolder>() {
    private val items = mutableListOf<TemplateItem>()
    inner class TemplateViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imgTemplate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TemplateViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_template, parent, false)
        return TemplateViewHolder(view)
    }

    override fun onBindViewHolder(holder: TemplateViewHolder, position: Int) {
        val template = items[position]
        Log.e("TemplateAdapter", "Loading image: ${template.image_url}")

        // Load image using Glide or Coil
        Glide.with(holder.itemView.context)
            .load(template.image_url)
            .into(holder.imageView)

        holder.itemView.setOnClickListener {
            onItemClick(template)
        }
    }

    override fun getItemCount(): Int = items.size

    fun setItems(newItems: List<TemplateItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
}
