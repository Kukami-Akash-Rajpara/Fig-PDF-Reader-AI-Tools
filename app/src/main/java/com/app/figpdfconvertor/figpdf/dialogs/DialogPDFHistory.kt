package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.figpdfconvertor.figpdf.databinding.DialogPdfHistoryBottomBinding
import com.app.figpdfconvertor.figpdf.model.PdfFileItem
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogPDFHistory(
    private val fileItem: PdfFileItem,
    private val listener: CardDialogListener
) : BottomSheetDialogFragment() {
    private lateinit var binding: DialogPdfHistoryBottomBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogPdfHistoryBottomBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
    }

    private fun setupUI() {
        // Thumbnail
        binding.imgThumbnail.setImageBitmap(fileItem.thumbnail)

        // Name
        binding.txtPDFname.setText(fileItem.name)
        binding.txtName.setText(fileItem.name)

        // Date & size
        val dateTime = DateFormat.format("dd/MM/yy • HH:mm", fileItem.createdTime)
        val sizeKb = "${fileItem.size / 1024} KB"
        binding.txtInfo.text = "$dateTime • $sizeKb"

        // Close button
        binding.imgClose.setOnClickListener {
            val newName = binding.txtPDFname.text.toString().trim()

            if (newName.isNotEmpty() && newName != fileItem.name) {
                listener.rename(fileItem, newName)  // ✅ notify adapter/activity
            }

            dismiss()
        }

        // Clear name
        binding.imgClear.setOnClickListener { binding.txtPDFname.setText("") }

        // Delete example
        binding.llSelect.setOnClickListener {
            listener.delete(fileItem)
            dismiss()
        }

        // Password UI
      /*  if (fileItem.isPasswordProtected) {
            binding.imgPwEye.visibility = View.VISIBLE
            binding.txtPw.visibility = View.VISIBLE
            binding.txtPw.text = fileItem.password
        } else {
            binding.imgPwEye.visibility = View.GONE
            binding.txtPw.visibility = View.GONE
        }*/
    }

    interface CardDialogListener {
        fun setPassword(fileItem: PdfFileItem)
        fun convert(fileItem: PdfFileItem)
        fun delete(fileItem: PdfFileItem)
        fun rename(fileItem: PdfFileItem, newName: String)
    }
}

