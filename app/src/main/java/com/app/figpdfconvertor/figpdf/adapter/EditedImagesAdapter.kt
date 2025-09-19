package com.app.figpdfconvertor.figpdf.adapter

import android.app.Activity
import android.graphics.drawable.Drawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.ConvertPDFActivity
import eightbitlab.com.blurview.BlurTarget
import eightbitlab.com.blurview.BlurView
import java.io.File
import java.util.*

class EditedImagesAdapter(
    private val context: Activity,
    private val items: MutableList<String>,   // 🔹 make mutable for delete
    private val onItemClick: (Int) -> Unit,
    private val onSelectionChanged: (Int) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val TYPE_IMAGE = 0
    private val TYPE_ADD = 1

    // 🔹 Selection tracking
    var selectionMode = false
    private val selectedItems = mutableSetOf<Int>()

    // 🔹 Always work with filtered list if selection mode is active
    private val actualItems: List<String>
        get() = if (selectionMode) items.filter { it != "ADD_IMAGE" } else items

    override fun getItemViewType(position: Int): Int {
        return if (actualItems[position] == "ADD_IMAGE") TYPE_ADD else TYPE_IMAGE
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == TYPE_IMAGE) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_convert_image, parent, false)
            ImageViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_add, parent, false)
            AddViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = actualItems[position]

        if (holder is ImageViewHolder) {
            Glide.with(holder.itemView.context)
                .load(item)
                .into(holder.imageView)

            val fileName = File(Uri.parse(item).path ?: item).name
            holder.txtName.text = fileName

            val radius = 10f
            val windowBackground: Drawable? = context.window.decorView.background
            holder.topBlurView.setupWith(holder.target)
                .setFrameClearDrawable(windowBackground)
                .setBlurRadius(radius)

            // 🔹 Show / hide checkbox depending on selectionMode
            holder.checkBox.visibility = if (selectionMode) View.VISIBLE else View.GONE
            holder.overlay.visibility = if (selectionMode) View.VISIBLE else View.GONE
            holder.checkBox.isChecked = selectedItems.contains(position)

            // Normal click
            holder.itemView.setOnClickListener {
                if (selectionMode) {
                    toggleSelection(position)
                    notifyItemChanged(position)
                } else {
                    onItemClick(position)
                }
            }

            // Long click → start selection mode
          /*  holder.itemView.setOnLongClickListener {
                if (!selectionMode) {
                    selectionMode = true
                    toggleSelection(position)
                    notifyDataSetChanged()
                    (context as? ConvertPDFActivity)?.onSelectionStarted()
                }
                true
            }*/
        }

        if (holder is AddViewHolder) {
            holder.itemView.setOnClickListener { onItemClick(position) }
        }
    }

    override fun getItemCount(): Int = actualItems.size

    class ImageViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imageView: ImageView = view.findViewById(R.id.image_item)
        val topBlurView: BlurView = view.findViewById(R.id.topBlurView)
        val target: BlurTarget = view.findViewById(R.id.target)
        val checkBox: CheckBox = view.findViewById(R.id.ck_select)
        val overlay: View = view.findViewById(R.id.selectionOverlay)
        val txtName: TextView = view.findViewById(R.id.txtName)
    }

    class AddViewHolder(view: View) : RecyclerView.ViewHolder(view)

    // 🔹 Selection helpers
    private fun toggleSelection(position: Int) {
        if (selectedItems.contains(position)) {
            selectedItems.remove(position)
        } else {
            selectedItems.add(position)
        }
        notifyItemChanged(position)
        onSelectionChanged(selectedItems.size)
    }

    fun clearSelection() {
        selectedItems.clear()
        selectionMode = false
        notifyDataSetChanged()
        onSelectionChanged(0)
    }

    fun getSelectedItems(): List<Int> = selectedItems.toList()

    fun deleteSelected() {
        val toDelete = getSelectedItems().sortedDescending()
        toDelete.forEach { index ->
            // careful: use actualItems mapping
            val itemToRemove = actualItems[index]
            items.remove(itemToRemove)
        }
        clearSelection()
        notifyDataSetChanged()
    }

    fun moveItem(from: Int, to: Int) {
        if (from < to) {
            for (i in from until to) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in from downTo to + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(from, to)
    }

    fun selectAll() {
        selectedItems.clear()
        // Only image items (skip ADD_IMAGE)
        for (i in actualItems.indices) {
            if (actualItems[i] != "ADD_IMAGE") {
                selectedItems.add(i)
            }
        }
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size)
    }

    // Check if all images are selected
    fun isAllSelected(): Boolean {
        val imageCount = actualItems.count { it != "ADD_IMAGE" }
        return selectedItems.size == imageCount && imageCount > 0
    }

    fun deselectAllKeepMode() {
        selectedItems.clear()
        notifyDataSetChanged()
    }

    fun getSelectedCount(): Int {
        return selectedItems.size  // assuming you track selection in a list or set
    }

    fun startSelectionMode(position: Int? = null) {
        if (!selectionMode) {
            selectionMode = true
            position?.let { toggleSelection(it) } // if you want to auto-select one item
            notifyDataSetChanged()
            (context as? ConvertPDFActivity)?.onSelectionStarted()
        }
    }
}
