package com.example.ui.ui.layout

import com.example.ui.BaseView
import com.example.ui.Container
import com.example.ui.PlatformView
import kotlinx.browser.document
import kotlinx.html.div
import kotlinx.html.dom.create
import kotlinx.html.js.img
import kotlinx.html.p

actual  class Frame : Container() {
    override val ele: PlatformView=
        document.createElement("div")
}

context(Frame)
        
actual fun <T : BaseView> T.widthPercent(float: Float) {
    document.create.img {
        src=""
        div { 

            p {

            }
        }
    }
}