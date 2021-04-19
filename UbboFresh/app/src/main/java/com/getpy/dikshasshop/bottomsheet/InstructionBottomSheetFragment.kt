package com.getpy.dikshasshop.bottomsheet

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doAfterTextChanged
import androidx.databinding.DataBindingUtil
import com.andrefrsousa.superbottomsheet.SuperBottomSheetFragment
import com.getpy.dikshasshop.R
import com.getpy.dikshasshop.UbboFreshApp
import com.getpy.dikshasshop.databinding.InstructionFragmentDemoSheetBinding

class InstructionBottomSheetFragment() : SuperBottomSheetFragment() {
    lateinit var binding:InstructionFragmentDemoSheetBinding
    @SuppressLint("SetTextI18n")
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        binding=DataBindingUtil.inflate(inflater,R.layout.instruction_fragment_demo_sheet, container, false)
        init()
        binding.close.setOnClickListener(View.OnClickListener {
           dismiss()
        })
        binding.addBut.setOnClickListener {
            UbboFreshApp.instance?.instructionString=binding.addInstEdit.text.toString()
            dismiss()
        }

        binding.addInstEdit.doAfterTextChanged {
            UbboFreshApp.instance?.instructionString=it.toString()
        }

        if(!TextUtils.isEmpty(UbboFreshApp.instance?.instructionString))
        {
            binding.addInstEdit.setText(UbboFreshApp.instance?.instructionString)
        }

        return binding.root
    }
    fun init()
    {
        binding.instDesc.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.addInstEdit.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.text.setTypeface(UbboFreshApp.instance?.latoregular)
        binding.addBut.setTypeface(UbboFreshApp.instance?.latoregular)
    }


}