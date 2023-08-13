package com.example.ui

import android.view.View
import android.view.ViewGroup
import com.example.ui.base.Color

actual typealias PlatformView = View


actual open class BaseView {
    actual open val ele: PlatformView = View(AndroidContext)

    
    actual fun width(float: Float) {
        ele.layoutParams = ele.layoutParams.apply {
            width = dp2px(float)
        }
    }

    
    actual fun height(float: Float) {
        ele.layoutParams = ele.layoutParams.apply {
            height = dp2px(float)
        }
    }

    
    actual fun marginTop(float: Float) {
        ele.layoutParams = (ele.layoutParams as ViewGroup.MarginLayoutParams).apply {
            topMargin = dp2px(float)
        }
    }

    
    actual fun marginBottom(float: Float) {
        ele.layoutParams = (ele.layoutParams as ViewGroup.MarginLayoutParams).apply {
            bottomMargin = dp2px(float)
        }
    }

    
    actual fun marginLeft(float: Float) {
        ele.layoutParams = (ele.layoutParams as ViewGroup.MarginLayoutParams).apply {
            leftMargin = dp2px(float)
        }
    }

    
    actual fun marginRight(float: Float) {
        ele.layoutParams = (ele.layoutParams as ViewGroup.MarginLayoutParams).apply {
            rightMargin = dp2px(float)
        }
    }

    
    actual fun paddingTop(float: Float) {
        ele.apply {
            setPadding(paddingLeft, dp2px(float), paddingRight, paddingBottom)
        }
    }

    
    actual fun paddingBottom(float: Float) {
        ele.apply {
            setPadding(paddingLeft, paddingTop, paddingRight, dp2px(float))
        }
    }

    
    actual fun paddingLeft(float: Float) {
        ele.apply {
            setPadding(dp2px(float), paddingTop, paddingRight, paddingBottom)
        }
    }

    
    actual fun paddingRight(float: Float) {
        ele.apply {
            setPadding(paddingLeft, paddingTop, paddingEnd, paddingBottom)
        }
    }

    actual fun widthMathParent() {
        ele.layoutParams = ele.layoutParams.apply {
            width = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    actual fun widthWrapContent() {
        ele.layoutParams = ele.layoutParams.apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    actual fun heightMathParent() {
        ele.layoutParams = ele.layoutParams.apply {
            height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    actual fun heightWrapContent() {
        ele.layoutParams = ele.layoutParams.apply {
            height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
    }

    
    actual fun marginVertical(float: Float) {
    }

    
    actual fun marginHorizontal(float: Float) {
    }

    
    actual fun paddingVertical(float: Float) {
    }

    
    actual fun paddingHorizontal(float: Float) {
    }

    
    actual fun margin(float: Float) {
    }

    
    actual fun padding(float: Float) {
    }

    
    actual fun radius(int: Int) {
    }

    
    actual fun radiusLeftTop(int: Int) {
    }

    
    actual fun radiusRightTop(int: Int) {
    }

    
    actual fun radiusRightBottom(int: Int) {
    }

    
    actual fun radiusLeftBottom(int: Int) {
    }

    actual fun stroke(dashWith: Int?, color: Color) {
    }

    actual fun backgroundBlurExceptAndroid(int: Int) {
    }

    actual fun dropShadow(int: Int) {
    }

    
    actual fun rotation(int: Int) {
    }

    
    actual fun scale(float: Float) {
    }

    
    actual fun transitionX(float: Float) {
    }

    
    actual fun transitionY(float: Float) {
    }

}