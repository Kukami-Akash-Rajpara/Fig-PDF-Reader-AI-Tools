package com.app.figpdfconvertor.figpdf.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.databinding.ItemPdfBinding
import com.app.figpdfconvertor.figpdf.model.PdfModel

class PdfAdapter(
    private val pdfList: List<PdfModel>,
    private val onChecked: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<PdfAdapter.PdfViewHolder>() {

    inner class PdfViewHolder(val binding: ItemPdfBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val binding = ItemPdfBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PdfViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val pdf = pdfList[position]
        holder.binding.fileName.text = pdf.name
        holder.binding.checkBox.isChecked = pdf.isSelected

        holder.binding.checkBox.setOnCheckedChangeListener { _, checked ->
            onChecked(position, checked)
        }
    }

    override fun getItemCount() = pdfList.size
}
