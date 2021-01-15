package com.astatineera.systemhelper

import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText

class UnsignedIntTextWatcher(private var editText: EditText?, private val allowEmptyField:Boolean) :TextWatcher {
    val zero = '0'
    val zero_s:String = zero.toString()
    val empty = ""

    init {
        when {
            editText!!.text.toString() == empty && !allowEmptyField -> editText!!.setText(zero_s)
        }
    }

    override fun beforeTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) = Unit

    override fun onTextChanged(charSequence: CharSequence?, i: Int, i1: Int, i2: Int) {
        if (editText!!.text.toString() == empty && i1 == 1) {
            when{
                allowEmptyField -> editText!!.setText(empty)
                else -> editText!!.setText(zero_s)
            }
            editText!!.setSelection(editText!!.length())
        }
    }

    override fun afterTextChanged(editable: Editable) {
        if (editable.toString().length > 1 &&
                editable.toString()[0] == zero) editable.delete(0, 1)
    }
}