package com.example.ui.ui.layout

import androidx.constraintlayout.widget.ConstraintLayout
import com.example.ui.PlatformView
import com.example.ui.AndroidContext
import com.example.ui.AndroidViewContainer
import com.example.ui.BaseView

actual  class Frame : AndroidViewContainer() {
    override val ele: PlatformView= ConstraintLayout(AndroidContext)
}


actual fun <T : BaseView> T.widthPercent(float: Float) {
    (ele.layoutParams as ConstraintLayout.LayoutParams).width
}