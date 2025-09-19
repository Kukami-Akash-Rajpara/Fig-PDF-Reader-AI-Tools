package com.app.figpdfconvertor.figpdf.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.utils.ImageFilterUtils

class FiltersAdapter(
    private val context: Context,
    private var baseBitmap: Bitmap,
    private val filters: List<String>,
    private val onFilterClick: (String) -> Unit,
) : RecyclerView.Adapter<FiltersAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val preview: ImageView = itemView.findViewById(R.id.filterPreview)
        val name: TextView = itemView.findViewById(R.id.filterName)
        val rlText: RelativeLayout = itemView.findViewById(R.id.rlText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_filter, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val filterName = filters[position]
        holder.name.text = filterName

        // Generate small preview with filter
        val previewBitmap = ImageFilterUtils.applyFilter(baseBitmap, filterName)

        holder.preview.setImageBitmap(previewBitmap)

        holder.itemView.setOnClickListener {
            onFilterClick(filterName)
        }
    }

    override fun getItemCount() = filters.size
    fun updateBaseBitmap(newBitmap: Bitmap) {
        baseBitmap = newBitmap
        notifyDataSetChanged()
    }


}

