package com.example.ui


actual abstract class Container : BaseView() {
    abstract override val ele:PlatformView
    actual fun addChild(baseView: BaseView){
        ele.appendChild(baseView.ele)
    }
}
