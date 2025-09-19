package com.app.figpdfconvertor.figpdf.adapter

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputType
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.dialogs.DialogPDFHistory
import com.app.figpdfconvertor.figpdf.model.PdfFileItem
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions
import com.google.android.material.textfield.TextInputEditText
import java.io.File

class PdfFileAdapter(
    private var mPdfOptions: ImageToPDFOptions? = null,
    private val items: MutableList<PdfFileItem>,
    private val context: Context,
    private val fragmentManager: androidx.fragment.app.FragmentManager,
    private val onShareClick: (PdfFileItem) -> Unit,
    private val onItemClick: (PdfFileItem) -> Unit,
) : RecyclerView.Adapter<PdfFileAdapter.PdfViewHolder>() {

    inner class PdfViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val imgThumbnail: ImageView = view.findViewById(R.id.imgThumbnail)
        val txtName: TextView = view.findViewById(R.id.txtName)
        val txtInfo: TextView = view.findViewById(R.id.txtInfo)
        val imgShare: ImageView = view.findViewById(R.id.imgShare)
        val imgMenu: ImageView = view.findViewById(R.id.imgMenu)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_saved_pdf, parent, false)
        return PdfViewHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: PdfViewHolder, position: Int) {
        val item = items[position]
        holder.txtName.text = item.name
        holder.imgThumbnail.setImageBitmap(item.thumbnail)
        val dateTime = DateFormat.format("dd/MM/yy • HH:mm", item.createdTime)
        val sizeKb = "${item.size / 1024} KB"

        // Combine with dot separator
        holder.txtInfo.text = "$dateTime • $sizeKb"

        holder.imgShare.setOnClickListener {
            onShareClick(item)
        }

        holder.itemView.setOnClickListener {
            onItemClick(item)
        }

        holder.imgMenu.setOnClickListener {
            val dialog = DialogPDFHistory(item, object : DialogPDFHistory.CardDialogListener {
                override fun setPassword(fileItem: PdfFileItem) {
                    // forward to activity
                    showSetPwDialog(context)
                }

                override fun convert(fileItem: PdfFileItem) {

                }

                override fun delete(fileItem: PdfFileItem) {
                    if (fileItem.file.exists()) {
                        val deleted = fileItem.file.delete()
                        if (deleted) {
                            android.widget.Toast.makeText(context, "${fileItem.name} deleted", android.widget.Toast.LENGTH_SHORT).show()

                            // remove from adapter list
                            val index = items.indexOf(fileItem)
                            if (index != -1) {
                                items.removeAt(index)
                                notifyItemRemoved(index)
                            }
                        } else {
                            android.widget.Toast.makeText(context, "Failed to delete ${fileItem.name}", android.widget.Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                override fun rename(fileItem: PdfFileItem, newName: String) {
                    val newFile = File(fileItem.file.parent, "$newName.pdf")

                    if (fileItem.file.exists()) {
                        val renamed = fileItem.file.renameTo(newFile)
                        if (renamed) {
                            Toast.makeText(context, "Renamed to $newName", Toast.LENGTH_SHORT).show()

                            // Update model
                            fileItem.name = "$newName.pdf"
                            fileItem.file = newFile

                            notifyItemChanged(holder.adapterPosition)
                        } else {
                            Toast.makeText(context, "Failed to rename", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            })
            dialog.show(fragmentManager, "PdfHistoryDialog")
        }


    }

    fun showSetPwDialog(context: Context) {
        // Inflate the custom layout
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_set_password, null)

        val etPassword = dialogView.findViewById<TextInputEditText>(R.id.etRename)
        val imgRemove = dialogView.findViewById<ImageView>(R.id.imgRemove)
        val okBtn = dialogView.findViewById<RelativeLayout>(R.id.okButton)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

// flag to track visibility
        val isPasswordVisible = booleanArrayOf(false)

        imgRemove.setOnClickListener(View.OnClickListener { v: View? ->
            if (isPasswordVisible[0]) {
                // Hide password
                etPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_PASSWORD
                )
                imgRemove.setImageResource(R.drawable.ic_pw_eye) // eye icon
                isPasswordVisible[0] = false
            } else {
                // Show password
                etPassword.setInputType(
                    InputType.TYPE_CLASS_TEXT or
                            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                )
                imgRemove.setImageResource(R.drawable.ic_pw_eye) // optional open eye icon
                isPasswordVisible[0] = true
            }
            // Move cursor to the end
            etPassword.setSelection(etPassword.getText()!!.length)
        })
        okBtn.setOnClickListener {
            val password = etPassword.text.toString().trim()
            mPdfOptions?.password = password
            mPdfOptions?.isPasswordProtected = password.isNotEmpty()

            Toast.makeText(
                context,
                if (password.isNotEmpty()) "Password set successfully" else "Password cleared",
                Toast.LENGTH_SHORT
            ).show()

            dialog.dismiss()
        }

        imgRemove.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

}

