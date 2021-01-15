package com.astatineera.systemhelper

import android.os.Build
import android.util.Size

class Size(private var height: Int, private var width: Int) {

    fun getHeight(): Int = height

    fun getWidth(): Int = width

    override fun toString(): String = getWidth().toString() + "x" + getHeight().toString()
}