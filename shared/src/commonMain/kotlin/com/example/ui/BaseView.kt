package com.example.ui

import com.example.ui.base.Color


@ViewDSL
expect open class BaseView() {
    val ele: PlatformView

    fun widthMathParent()
    fun widthWrapContent()
    fun heightMathParent()
    fun heightWrapContent()

    
    fun width(float: Float)

    
    fun height(float: Float)


    
    fun marginTop(float: Float)

    
    fun marginBottom(float: Float)

    
    fun marginLeft(float: Float)

    
    fun marginRight(float: Float)

    
    fun paddingTop(float: Float)

    
    fun paddingBottom(float: Float)

    
    fun paddingLeft(float: Float)

    
    fun paddingRight(float: Float)

    
    fun marginVertical(float: Float)

    
    fun marginHorizontal(float: Float)

    
    fun paddingVertical(float: Float)

    
    fun paddingHorizontal(float: Float)

    
    fun margin(float: Float)

    
    fun padding(float: Float)

    
    fun radius(int: Int)

    
    fun radiusLeftTop(int: Int)

    
    fun radiusRightTop(int: Int)

    
    fun radiusRightBottom(int: Int)

    
    fun radiusLeftBottom(int: Int)

    fun stroke(dashWith: Int?, color: Color)

    fun backgroundBlurExceptAndroid(int: Int)
    fun dropShadow(int: Int)

    
    fun rotation(int: Int)
    
    fun scale(float: Float)
    
    fun transitionX(float: Float)
    
    fun transitionY(float: Float)
}
