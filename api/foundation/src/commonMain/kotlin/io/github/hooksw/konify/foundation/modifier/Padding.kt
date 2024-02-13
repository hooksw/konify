package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.unit.Dp


data class PaddingAttr(
    var top: Dp = Dp.Unspecified,
    var right: Dp = Dp.Unspecified,
    var bottom: Dp = Dp.Unspecified,
    var left: Dp = Dp.Unspecified,
)

class PaddingElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<PaddingAttr>(modifier, update) {
    override fun PaddingAttr.updateFrom(other: PaddingAttr) {
        top = other.top
        right = other.right
        bottom = other.bottom
        left = other.left
    }

    override fun createAttr(): PaddingAttr = PaddingAttr()
}

interface PaddingScope {
    var top: Dp
    var right: Dp
    var bottom: Dp
    var left: Dp
    fun vertical(value: Dp)
    fun horizontal(value: Dp)
    fun all(value: Dp)
}

private class PaddingScopeImpl : PaddingScope {
    lateinit var paddingElement: PaddingElement
    override var top: Dp by paddingElement.activeAttr::top
    override var right: Dp by paddingElement.activeAttr::right
    override var bottom: Dp by paddingElement.activeAttr::bottom
    override var left: Dp by paddingElement.activeAttr::left

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

fun Modifier.padding(call: PaddingScope.() -> Unit) {
    val scope = PaddingScopeImpl()
    val ele = PaddingElement(this) {
        scope.call()
    }
    scope.paddingElement = ele
    append(ele)
}