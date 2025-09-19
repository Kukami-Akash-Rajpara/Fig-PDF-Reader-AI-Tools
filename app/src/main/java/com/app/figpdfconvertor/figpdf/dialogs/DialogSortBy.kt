package com.app.figpdfconvertor.figpdf.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.app.figpdfconvertor.figpdf.R
import com.app.figpdfconvertor.figpdf.databinding.DialogSortByBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class DialogSortBy(
    private val name: String,
    private val listener: CardDialogListener,
) : BottomSheetDialogFragment() {

    private lateinit var binding: DialogSortByBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = DialogSortByBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addCardsToView()
    }

    private fun addCardsToView() {
        binding.tvTitle.text = name
        binding.llName.setOnClickListener {
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
        }
//        binding.cardContainer.addView(newCardView)
    }

    interface CardDialogListener {
        fun setName()
        fun setCreatedDate()
        fun setModifiedDate()
        fun setAscending()
        fun setDescending()
    }
}
