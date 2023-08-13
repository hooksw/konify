package com.example.ui

import android.view.ViewGroup
import android.widget.FrameLayout


abstract class AndroidViewContainer : BaseView(), Container {
    abstract override val ele: PlatformView
    override fun addChild(baseView: BaseView) {
        (ele as ViewGroup).addView(baseView.ele)
        FrameLayout(AndroidContext)
    }
}