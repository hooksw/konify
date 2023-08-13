package com.example.ui

import android.annotation.SuppressLint
import android.content.Context

@SuppressLint("StaticFieldLeak")
private lateinit var _context:Context
fun configContext(context: Context){
    _context=context
}
val AndroidContext:Context
    get() = _context