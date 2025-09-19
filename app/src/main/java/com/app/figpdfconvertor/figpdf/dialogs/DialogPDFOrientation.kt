package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.DialogPdfComponentBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogPdfOrientationBinding
import com.app.figpdfconvertor.figpdf.databinding.DialogSortByBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogPDFOrientation(
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogPdfOrientationBinding
    private var selectedOption: String = "Auto"
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogPdfOrientationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
    }

    private fun addCardsToView() {
        selectOnly(binding.imgAuto, "Auto")
        binding.llAuto.setOnClickListener { selectOnly(binding.imgAuto, "Auto") }
        binding.llPortrait.setOnClickListener { selectOnly(binding.imgPortrait, "Portrait") }
        binding.llLandscape.setOnClickListener { selectOnly(binding.imgLandscape, "Landscape") }

        binding.btnOk.setOnClickListener {
            listener.onPageOrientationSelected(selectedOption)
            dismiss()
        }

        // Cancel
        binding.btnCancel.setOnClickListener {
            dismiss()
        }
    }


    private fun selectOnly(selectedView: View, option: String) {
        // reset all to unselected
        binding.imgAuto.isSelected = false
        binding.imgPortrait.isSelected = false
        binding.imgLandscape.isSelected = false

        // set selected
        selectedView.isSelected = true
        selectedOption = option
    }

    interface CardDialogListener {
        fun onPageOrientationSelected(option: String)
    }
}
