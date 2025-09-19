package com.app.figpdfconvertor.figpdf.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.model.PdfPickerItem

class PdfPickerAdapter(
    private val items: MutableList<PdfPickerItem>,
    private val onPickClick: (position: Int) -> Unit
) : RecyclerView.Adapter<PdfPickerAdapter.PdfPickerViewHolder>() {

    inner class PdfPickerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val pickBtn: LinearLayout = itemView.findViewById(R.id.pickPdfBtn)
        val pdfNameText = itemView.findViewById<TextView>(R.id.pdfNameText)
        val txtUploadData = itemView.findViewById<TextView>(R.id.txtUploadData)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfPickerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pdf_picker, parent, false)
        return PdfPickerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfPickerViewHolder, position: Int) {
        val item = items[position]
        holder.pdfNameText.text = item.name ?: "No file selected"
        holder.txtUploadData.visibility = if (item.uri != null) View.GONE else View.VISIBLE
        holder.pickBtn.setOnClickListener { onPickClick(position) }
    }

    override fun getItemCount(): Int = items.size
}
