package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.graphics.Color
import io.github.hooksw.konify.runtime.unit.Dp

interface DecorationHandler {
    fun setBackgroundColor(color: Color){

    }
    fun setBorderWith(width:Dp){

    }
    fun setBorderColor(color: Color){

    }
    fun setTopLeftRadius(radius:Float){

    }
    fun setTopRightRadius(radius:Float){

    }
    fun setBottomLeftRadius(radius:Float){

    }
    fun setBottomRightRadius(radius:Float){

    }

}