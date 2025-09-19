package com.app.figpdfconvertor.figpdf.adapter

import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.model.PdfFileItem

class TxtFileAdapter(
    private val items: List<PdfFileItem>,
    private val onClick: (PdfFileItem) -> Unit
) : RecyclerView.Adapter<TxtFileAdapter.TxtViewHolder>() {

    inner class TxtViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtName: TextView = itemView.findViewById(R.id.txtName)
        val txtSize: TextView = itemView.findViewById(R.id.txtInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TxtViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_txt_file, parent, false)
        return TxtViewHolder(view)
    }

    override fun onBindViewHolder(holder: TxtViewHolder, position: Int) {
        val item = items[position]
        // Read the first few lines of the TXT file for preview
        val fileContent = try {
            val file = item.file
            if (file.exists()) {
                file.readLines().take(2).joinToString("\n") // Only first 2 lines
            } else ""
        } catch (e: Exception) {
            e.printStackTrace()
            ""
        }

        holder.txtName.text = fileContent

        // Copy button
        holder.itemView.findViewById<View>(R.id.imgCopy).setOnClickListener {
            try {
                val textToCopy = item.file.readText() // Copy full text
                val clipboard = holder.itemView.context.getSystemService(android.content.ClipboardManager::class.java)
                val clip = android.content.ClipData.newPlainText("TXT Content", textToCopy)
                clipboard.setPrimaryClip(clip)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Delete button
        holder.itemView.findViewById<View>(R.id.imgDelete).setOnClickListener {
            try {
                if (item.file.exists() && item.file.delete()) {
                    // Optional: Remove item from list and notify adapter
                    (items as MutableList).removeAt(position)
                    notifyItemRemoved(position)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Optional: Click on the whole item to open/share the file
        holder.itemView.setOnClickListener {
            onClick(item)
        }
        val dateTime = DateFormat.format("dd/MM/yy • HH:mm", item.createdTime)
        val sizeKb = "${item.size / 1024} KB"

        // Combine with dot separator
        holder.txtSize.text = "$dateTime • $sizeKb"
    }


    override fun getItemCount(): Int = items.size
}
