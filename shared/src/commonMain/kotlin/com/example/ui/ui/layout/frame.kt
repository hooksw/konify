package com.example.ui.ui.layout

import com.example.ui.BaseView
import com.example.ui.Container
import com.example.ui.add

expect class Frame() :BaseView, Container {

}

enum class Gravity {
    TopCenter, TopLeft, TopRight,
    BottomCenter, BottomLeft, BottomRight,
    Center, CenterLeft, CenterRight,
}

fun Container.Frame(builder: Frame.() -> Unit) {
    add(Frame(), builder)
}

//context(FrameView)

expect  fun <T : BaseView> T.widthPercent(float: Float)

//context(FrameView)

expect fun <T : BaseView> T.heightPercent(float: Float)

//context(FrameView)

expect fun <T : BaseView> T.gravity(gravity: Gravity)