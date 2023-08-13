package com.example

import com.example.ui.base.Color

interface Modifier{
    val width:Float
    val height:Float
    val rotation:Float
    val transformX:Float
    val transformY:Float
    val scaleX:Float
    val scaleY:Float
}

interface Decoration{

    val topLeftRadius:Float
    val topRightRadius:Float
    val bottomLeftRadius:Float
    val bottomRightRadius:Float
    val borderSize:Int
    val borderColor:Color
    val fillColor:Int
    val dropShadow:Int
}
fun Modifier.width(){

}