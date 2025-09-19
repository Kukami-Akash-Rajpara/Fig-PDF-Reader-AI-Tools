package com.app.figpdfconvertor.figpdf.adapter

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.app.figpdfconvertor.figpdf.databinding.ItemPagerImageBinding
import androidx.core.net.toUri

class ViewPagerAdapter(
    private val context: Context,
    private val imageUris: ArrayList<String>,
) : RecyclerView.Adapter<ViewPagerAdapter.MyViewHolder>() {

    // Store edited images (null = not edited yet)
    private val bitmaps = MutableList<Bitmap?>(imageUris.size) { null }

    inner class MyViewHolder(val binding: ItemPagerImageBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemPagerImageBinding.inflate(LayoutInflater.from(context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val bmp = bitmaps[position]

        if (bmp != null) {
            // ✅ Show edited image
            holder.binding.imgPreview.setImageBitmap(bmp)
        } else {
            // ✅ Load original only if not edited
            Glide.with(context)
                .load(imageUris[position].toUri())
                .into(holder.binding.imgPreview)
        }
    }

    override fun getItemCount(): Int = imageUris.size

    // ✅ Call this when user clicks "Done" after editing
    fun updateBitmapAt(position: Int, bmp: Bitmap) {
        bitmaps[position] = bmp
        notifyItemChanged(position) // refresh only that page
    }

    // ✅ Get latest image (edited or original)
    fun getBitmapAt(position: Int, context: Context): Bitmap? {
        return bitmaps[position] ?: run {
            // If not edited yet, load original
            val uri = Uri.parse(imageUris[position])
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    }

    fun removeAt(position: Int) {
        if (position in 0 until imageUris.size) {
            imageUris.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, imageUris.size)
        }
    }

    fun updateUriAt(position: Int, newUri: String) {
        if (position in imageUris.indices) {
            imageUris[position] = newUri
            notifyItemChanged(position)
        }
    }
}


