package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.DialogChooseOptionBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class ChooseCardDialogFragment(
    private val name: String,
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogChooseOptionBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogChooseOptionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
    }

    private fun addCardsToView() {
        binding.tvTitle.text = name
        binding.llSaveGallery.setOnClickListener {
            listener.saveToGallery()
            dismiss()
        }
        binding.llSetPw.setOnClickListener {
            listener.setPwClick()
            dismiss()
        }

        binding.llSelect.setOnClickListener {
            listener.select()
            dismiss()
        }
        binding.llSortBy.setOnClickListener {
            listener.sortBy()
            dismiss()
        }

        binding.cardContainer.setOnClickListener {
            dismiss()
        }
//        binding.cardContainer.addView(newCardView)
    }

    interface CardDialogListener {
        fun setPwClick()
        fun saveToGallery()
        fun select()
        fun sortBy()
    }
}
