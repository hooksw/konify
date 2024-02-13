package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.dp

data class SizeAttr(
    var width: Dp = Match_Parent,
    var height: Dp = Wrap_Content,
    var maxWidth: Dp = Dp.Unspecified,
    var minWidth: Dp = Dp.Unspecified,
    var maxHeight: Dp = Dp.Unspecified,
    var minHeight: Dp = Dp.Unspecified,
)

class SizeAttrElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<SizeAttr>(modifier, update) {
    override fun createAttr(): SizeAttr = SizeAttr()

    override fun SizeAttr.updateFrom(other: SizeAttr) {
        width = other.width
        height = other.height
        maxWidth = other.maxWidth
        maxHeight = other.maxHeight
        minWidth = other.minWidth
        minHeight = other.minHeight
    }

}

interface SizeScope {
    var width: Dp
    var height: Dp
    var maxWidth: Dp
    var minWidth: Dp
    var maxHeight: Dp
    var minHeight: Dp
}


private class SizeScopeImpl : SizeScope {
    lateinit var sizeAttrElement: SizeAttrElement
    override var width: Dp by sizeAttrElement.activeAttr::width
    override var height: Dp by sizeAttrElement.activeAttr::height
    override var maxWidth: Dp by sizeAttrElement.activeAttr::maxWidth
    override var minWidth: Dp by sizeAttrElement.activeAttr::minWidth
    override var maxHeight: Dp by sizeAttrElement.activeAttr::maxHeight
    override var minHeight: Dp by sizeAttrElement.activeAttr::minHeight
}

fun Modifier.size(call: SizeScope.() -> Unit) {
    val scope = SizeScopeImpl()
    val ele = SizeAttrElement(this) {
        scope.call()
    }
    scope.sizeAttrElement = ele
    append(ele)
}


val Match_Parent = (-1).dp
val Wrap_Content = (-2).dp
