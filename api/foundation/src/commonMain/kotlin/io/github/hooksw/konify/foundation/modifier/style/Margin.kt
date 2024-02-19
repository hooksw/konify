package io.github.hooksw.konify.foundation.modifier.style

import io.github.hooksw.konify.foundation.modifier.AttrElement
import io.github.hooksw.konify.foundation.modifier.Modifier
import io.github.hooksw.konify.foundation.modifier.append
import io.github.hooksw.konify.foundation.unit.Dp


data class MarginAttr(
    var top: Dp = Dp.Unspecified,
    var right: Dp = Dp.Unspecified,
    var bottom: Dp = Dp.Unspecified,
    var left: Dp = Dp.Unspecified,
)

class MarginElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<MarginAttr>(modifier, update) {
    override fun MarginAttr.updateFrom(other: MarginAttr) {
        top = other.top
        right = other.right
        bottom = other.bottom
        left = other.left
    }

    override fun createAttr(): MarginAttr = MarginAttr()
}

interface MarginScope {
    var top: Dp
    var right: Dp
    var bottom: Dp
    var left: Dp
    fun vertical(value: Dp)
    fun horizontal(value: Dp)
    fun all(value: Dp)
}

private class MarginScopeImpl : MarginScope {
    lateinit var marginElement: MarginElement
    override var top: Dp by marginElement.activeAttr::top
    override var right: Dp by marginElement.activeAttr::right
    override var bottom: Dp by marginElement.activeAttr::bottom
    override var left: Dp by marginElement.activeAttr::left

    override fun vertical(value: Dp) {
        top = value
        bottom = value
    }

    override fun horizontal(value: Dp) {
        left = value
        right = value
    }

    override fun all(value: Dp) {
        top = value
        bottom = value
        left = value
        right = value
    }

}

fun Modifier.margin(call: MarginScope.() -> Unit): Modifier {
    val scope = MarginScopeImpl()
    val ele = MarginElement(this) {
        scope.call()
    }
    scope.marginElement = ele
   return this append ele
}
