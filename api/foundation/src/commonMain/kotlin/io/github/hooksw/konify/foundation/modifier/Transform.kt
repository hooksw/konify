package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.Unspecified

class TransformAttr(
    var rotation: Float = Float.Unspecified,
    var scaleX: Float = Float.Unspecified,
    var scaleY: Float = Float.Unspecified,
    var translateX: Dp = Dp.Unspecified,
    var translateY: Dp = Dp.Unspecified,
)

class TransformElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<TransformAttr>(modifier, update) {
    override fun createAttr(): TransformAttr = TransformAttr()

    override fun TransformAttr.updateFrom(other: TransformAttr) {
        rotation = other.rotation
        scaleX = other.scaleX
        scaleY = other.scaleY
        translateX = other.translateX
        translateY = other.translateY
    }

}

interface TransformScope {
    var rotation: Float
    var scaleX: Float
    var scaleY: Float
    var translateX: Dp
    var translateY: Dp
}


private class TransformScopeImpl : TransformScope {
    lateinit var transformElement: TransformElement
    override var rotation: Float by transformElement.activeAttr::rotation
    override var scaleX: Float by transformElement.activeAttr::scaleX
    override var scaleY: Float by transformElement.activeAttr::scaleY
    override var translateX: Dp by transformElement.activeAttr::translateX
    override var translateY: Dp by transformElement.activeAttr::translateY

}

fun Modifier.transform(call: TransformScope.() -> Unit) {
    val scope = TransformScopeImpl()
    val ele = TransformElement(this) {
        scope.call()
    }
    scope.transformElement = ele
    append(ele)
}
