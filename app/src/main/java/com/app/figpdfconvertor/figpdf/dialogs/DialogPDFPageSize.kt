package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.figpdfconvertor.figpdf.databinding.DialogPdfPageSizeBinding
import com.app.figpdfconvertor.figpdf.utils.PageSizeUtils
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogPDFPageSize(
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {
    private var hasUserSelected = false

    private lateinit var binding: DialogPdfPageSizeBinding
    private var selectedOption: String = "A4"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogPdfPageSizeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
        selectDefaultIfNone()
    }
    private fun selectDefaultIfNone() {
        if (!hasUserSelected) {
            // Use value from PageSizeUtils if set, else fallback to "A4"
            val defaultOption = PageSizeUtils.mPageSize ?: "A4"

            when (defaultOption) {
                "Auto Fit" -> selectOnly(binding.imgName, "Auto Fit")
                "A3" -> selectOnly(binding.imgCreated, "A3")
                "A4" -> selectOnly(binding.imgModify, "A4")
                "A5" -> selectOnly(binding.imgAscending, "A5")
                "B4" -> selectOnly(binding.imgDescending, "B4")
                "B5" -> selectOnly(binding.imgB5, "B5")
                "Letter" -> selectOnly(binding.imgLetter, "Letter")
                "Legal" -> selectOnly(binding.imgLegal, "Legal")
                "Executive" -> selectOnly(binding.imgExecutive, "Executive")
                "Business Card" -> selectOnly(binding.imgBusinessCard, "Business Card")
            }
        }
    }

    private fun addCardsToView() {
        binding.llCreated.setOnClickListener { selectOnly(binding.imgCreated, "A3") }
        binding.llModify.setOnClickListener { selectOnly(binding.imgModify, "A4") }
        binding.llAscending.setOnClickListener { selectOnly(binding.imgAscending, "A5") }
        binding.llDescending.setOnClickListener { selectOnly(binding.imgDescending, "B4") }
        binding.llName.setOnClickListener { selectOnly(binding.imgName, "Auto Fit") }
        binding.imgB5.setOnClickListener { selectOnly(binding.imgB5, "B5") }
        binding.imgLetter.setOnClickListener { selectOnly(binding.imgLetter, "Letter") }
        binding.imgLegal.setOnClickListener { selectOnly(binding.imgLegal, "Legal") }
        binding.imgExecutive.setOnClickListener { selectOnly(binding.imgExecutive, "Executive") }
        binding.imgBusinessCard.setOnClickListener { selectOnly(binding.imgBusinessCard, "Business Card") }

        // ✅ OK button
        binding.btnOk.setOnClickListener {

            listener.onPageSizeSelected(selectedOption)
            dismiss()
        }

        // Cancel
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }

    private fun selectOnly(selectedView: View, option: String) {
        // reset all to unselected
        binding.imgName.isSelected = false
        binding.imgCreated.isSelected = false
        binding.imgModify.isSelected = false
        binding.imgAscending.isSelected = false
        binding.imgDescending.isSelected = false
        binding.imgB5.isSelected = false
        binding.imgLetter.isSelected = false
        binding.imgLegal.isSelected = false
        binding.imgExecutive.isSelected = false
        binding.imgBusinessCard.isSelected = false

        // set selected
        selectedView.isSelected = true
        selectedOption = option
        hasUserSelected = true

        // Update the PageSizeUtils singleton
        PageSizeUtils.mPageSize = option
    }


    interface CardDialogListener {
        fun onPageSizeSelected(option: String)
    }
}
