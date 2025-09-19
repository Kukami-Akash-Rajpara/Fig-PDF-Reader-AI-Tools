package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.activity.ConvertPDFActivity
import com.app.figpdfconvertor.figpdf.databinding.DialogPdfComponentBinding
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions.PdfOrientation
import com.app.figpdfconvertor.figpdf.utils.PageSizeUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlin.toString

class DialogPDFComponent(
    private val name: String,
    private val pdfOption: ImageToPDFOptions,
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogPdfComponentBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogPdfComponentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
    }

    private fun addCardsToView() {
        binding.txtPDFname.setText(name).toString().trim()
        if (pdfOption != null) {
            if (!pdfOption.isPasswordProtected) {
                // PDF is not password protected
                binding.imgPwSelect.setImageResource(R.drawable.ic_square_deselect)

            } else {
                // PDF is password protected
                binding.imgPwSelect.setImageResource(R.drawable.custom_checkbox_checked)

            }
        }

        if (pdfOption != null) {
            if (!pdfOption.isWhiteMargin) {
                // PDF is not password protected
                binding.imgMarginSelect.setImageResource(R.drawable.ic_square_deselect)

            } else {
                // PDF is password protected
                binding.imgMarginSelect.setImageResource(R.drawable.custom_checkbox_checked)

            }
        }

        binding.imgClear.setOnClickListener {
            binding.txtPDFname.setText("")
        }

        binding.rlConvert.setOnClickListener {
            val userEnteredName = binding.txtPDFname.text.toString().trim()

            // Update PDF options
            pdfOption.outFileName = if (userEnteredName.isNotEmpty()) userEnteredName else pdfOption.outFileName

            listener.convert()
            dismiss()
        }

        binding.rlPreview.setOnClickListener {
            (activity as? ConvertPDFActivity)?.previewPdf()
            dismiss()
        }

        binding.txtPDFname.setHint(name).toString().trim()

        binding.txtPageSize.text = PageSizeUtils.mPageSize
        binding.txtCompress.text = pdfOption.mCompression

        binding.txtOrientation.text = pdfOption.pdfOrientation.toString()

        binding.txtPDFname.setText(
            if (pdfOption.outFileName.isNullOrEmpty()) name else pdfOption.outFileName
        )
        binding.txtPDFname.hint = name


        binding.llCompress.setOnClickListener {
            //   listener.setCompress()

            val compressDialog =
                DialogPDFCompress(pdfOption, object : DialogPDFCompress.CardDialogListener {
                    override fun onCompressionSelected(option: String) {
                        // ✅ Set selected option into txtCompress
                        binding.txtCompress.text = option
                    }
                })
            compressDialog.show(parentFragmentManager, "compressDialog")

            dismiss()
        }

        binding.rlPage.setOnClickListener {
            //listener.setPage()
            val pageSizeDialog = DialogPDFPageSize(object : DialogPDFPageSize.CardDialogListener {
                override fun onPageSizeSelected(option: String) {
                    binding.txtPageSize.text = pdfOption.pageSize // update selected text
                }
            })
            pageSizeDialog.show(parentFragmentManager, "pageSizeDialog")
            dismiss()
        }

        binding.rlOrientation.setOnClickListener {
            val pageSizeDialog = DialogPDFOrientation(object : DialogPDFOrientation.CardDialogListener {
                override fun onPageOrientationSelected(option: String) {
                    binding.txtOrientation.text = option // update selected text

                    // Map String to PdfOrientation enum
                    val orientation = when (option) {
                        "Portrait" -> PdfOrientation.PORTRAIT
                        "Landscape" -> PdfOrientation.LANDSCAPE
                        else -> PdfOrientation.AUTO
                    }

                    // Set orientation in PDF options
                    pdfOption?.pdfOrientation = orientation
                }
            })
            pageSizeDialog.show(parentFragmentManager, "pageSizeDialog")
            dismiss()
        }

        binding.imgPwSelect.setOnClickListener {
            listener.setPassword()
            dismiss()
        }

        binding.imgMarginSelect.setOnClickListener {
            pdfOption?.toggleWhiteMargin() // Toggle state

            // Update the icon immediately
            val isEnabled = pdfOption?.isWhiteMargin() == true
            binding.imgMarginSelect.setImageResource(
                if (isEnabled) R.drawable.custom_checkbox_checked
                else R.drawable.ic_square_deselect
            )

            // Show a toast
            val status = if (isEnabled) "enabled" else "disabled"
            Toast.makeText(context, "White margin $status", Toast.LENGTH_SHORT).show()
        }

        binding.imgClose.setOnClickListener {
            dismiss()
        }

        binding.txtPw.text = pdfOption.password

        if (pdfOption.isPasswordProtected) {
            binding.imgPwEye.visibility = View.VISIBLE
            binding.txtPw.visibility = View.VISIBLE
        } else {
            binding.imgPwEye.visibility = View.GONE
            binding.txtPw.visibility = View.GONE
        }


    }

    interface CardDialogListener {
        /* fun setPage()
         fun setOrientation()
         fun setDescending()*/
        fun setPassword()
        fun convert()
    }
}