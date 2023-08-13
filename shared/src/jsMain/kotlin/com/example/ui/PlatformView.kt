package com.example.ui

import com.example.ui.base.Color
import kotlinx.browser.document
import org.w3c.dom.Element

@Suppress("ACTUAL_WITHOUT_EXPECT")
actual typealias PlatformView = Element


actual open class BaseView {
    actual open val ele: PlatformView = document.createElement("div")

    actual fun widthMathParent() {
    }

    actual fun widthWrapContent() {
    }

    actual fun heightMathParent() {
    }

    actual fun heightWrapContent() {
    }

    
    actual fun width(float: Float) {
    }

    
    actual fun height(float: Float) {
    }

    
    actual fun marginTop(float: Float) {
    }

    
    actual fun marginBottom(float: Float) {
    }

    
    actual fun marginLeft(float: Float) {
    }

    
    actual fun marginRight(float: Float) {
    }

    
    actual fun paddingTop(float: Float) {
    }

    
    actual fun paddingBottom(float: Float) {
    }

    
    actual fun paddingLeft(float: Float) {
    }

    
    actual fun paddingRight(float: Float) {
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