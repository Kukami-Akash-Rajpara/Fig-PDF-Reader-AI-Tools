package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.app.figpdfconvertor.figpdf.databinding.DialogCompressSettingBinding
import com.app.figpdfconvertor.figpdf.utils.ImageToPDFOptions
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogPDFCompress(
    private val pdfOption: ImageToPDFOptions,
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogCompressSettingBinding
    private var selectedOption: String = "LOW"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogCompressSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
    }

    private fun addCardsToView() {
        selectOnly(binding.imgLow, "LOW")

        binding.imgMax.setOnClickListener { selectOnly(binding.imgMax, "MAX") }
        binding.imgMedium.setOnClickListener { selectOnly(binding.imgMedium, "MEDIUM") }
        binding.imgRegular.setOnClickListener { selectOnly(binding.imgRegular, "REGULAR") }
        binding.imgLow.setOnClickListener { selectOnly(binding.imgLow, "LOW") }

        // OK button click → return selected option
        binding.rlOk.setOnClickListener {
            pdfOption.mCompression = selectedOption
            Toast.makeText(context, "Selected: $selectedOption", Toast.LENGTH_SHORT).show()
            listener.onCompressionSelected(selectedOption)
            dismiss()
        }

        binding.imgClose.setOnClickListener { dismiss() }
       /* binding.llName.setOnClickListener {
            listener.setName()
            dismiss()
        }
        binding.llCreated.setOnClickListener {
            listener.setCreatedDate()
            dismiss()
        }

        binding.llModify.setOnClickListener {
            listener.setModifiedDate()
            dismiss()
        }
        binding.llAscending.setOnClickListener {
            listener.setAscending()
            dismiss()
        }
        binding.llDescending.setOnClickListener {
            listener.setDescending()
            dismiss()
        }*/
//        binding.cardContainer.addView(newCardView)
    }
    private fun selectOnly(selectedView: View, option: String) {
        binding.imgMax.isSelected = false
        binding.imgMedium.isSelected = false
        binding.imgRegular.isSelected = false
        binding.imgLow.isSelected = false

        selectedView.isSelected = true
        selectedOption = option
    }
    interface CardDialogListener {
      /*  fun setName()
        fun setCreatedDate()
        fun setModifiedDate()
        fun setAscending()
        fun setDescending()*/
        fun onCompressionSelected(option: String)
    }


}
