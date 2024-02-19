package io.github.hooksw.konify.foundation.layout

import io.github.hooksw.konify.foundation.modifier.LayoutDirection
import io.github.hooksw.konify.foundation.modifier.Modifier
import io.github.hooksw.konify.foundation.modifier.ModifierImpl
import io.github.hooksw.konify.foundation.modifier.UiConfigurationElement
import io.github.hooksw.konify.foundation.unit.Density
import io.github.hooksw.konify.foundation.unit.Dp

interface UiElement {
    var layoutDirection: LayoutDirection
    var density: Density
    val modifier: Modifier
}

interface LayoutHandler {
    fun handle(node: LayoutUiNode)
    fun layout(x: Dp, y: Dp)
}

class LayoutUiNode() : UiElement {

    override var layoutDirection: LayoutDirection = TODO()
        get() = directionFromModifier
    override var density: Density


    private val directionFromModifier: LayoutDirection
        get() {
            var reached = false
            if (modifier != Modifier) {
                (modifier as ModifierImpl).foreachElement {
                    if(it is UiConfigurationElement&&it.)
                }
            }
        }
    private var parentNode: LayoutUiNode? = null

    var calculatedWidth: Dp = Dp.Unspecified
    var calculatedHeight: Dp = Dp.Unspecified

    override val modifier: Modifier = Modifier

    var state: LayoutState = LayoutState.Idel

    enum class LayoutState {
        Idel,
    }

}

interface MeasurePolicy {
    var width: Dp
    var width: Dp
}