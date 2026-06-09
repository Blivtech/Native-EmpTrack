package com.blivtech.emptrack.ui.employee.bottomsheet

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.blivtech.emptrack.databinding.BottomsheetGenderBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GenderBottomSheet(
    private val selectedGender: Int? = null,
    private val onSelected: (Int, String) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: BottomsheetGenderBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = BottomsheetGenderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // ✅ Show check on pre-selected
        when (selectedGender) {
            1 -> binding.ivMaleCheck.visibility = View.VISIBLE
            2 -> binding.ivFemaleCheck.visibility = View.VISIBLE
            3 -> binding.ivOtherCheck.visibility = View.VISIBLE
        }

        binding.layoutMale.setOnClickListener {
            onSelected(1, "Male")
            dismiss()
        }
        binding.layoutFemale.setOnClickListener {
            onSelected(2, "Female")
            dismiss()
        }
        binding.layoutOther.setOnClickListener {
            onSelected(3, "Other")
            dismiss()
        }
    }
}