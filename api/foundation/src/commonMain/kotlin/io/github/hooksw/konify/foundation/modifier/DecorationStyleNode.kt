package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp

data class DecorationStyleAttr(
    var topLeftRadius: Dp = Dp.Unspecified,
    var topRightRadius: Dp = Dp.Unspecified,
    var bottomLeftRadius: Dp = Dp.Unspecified,
    var bottomRightRadius: Dp = Dp.Unspecified,
    val topBorder: Border = Border(),
    val rightBorder: Border = Border(),
    val bottomBorder: Border = Border(),
    val leftBorder: Border = Border(),
)

enum class BorderStyle {
    Dotted, Dashed, Solid
}

data class Border(
    var borderWith: Dp = Dp.Unspecified,
    var borderStyle: BorderStyle = BorderStyle.Solid
)

abstract class DecorationStyleNode : AttrElement<DecorationStyleAttr>() {
    override val finalAttr: DecorationStyleAttr = DecorationStyleAttr()
    override val activeAttr: DecorationStyleAttr = DecorationStyleAttr()
    override fun update() {
        finalAttr.topLeftRadius = activeAttr.topLeftRadius
        finalAttr.topRightRadius = activeAttr.topRightRadius
        finalAttr.bottomLeftRadius = activeAttr.bottomLeftRadius
        finalAttr.bottomRightRadius = activeAttr.bottomRightRadius
        finalAttr.topBorder.borderStyle = activeAttr.topBorder.borderStyle
        finalAttr.topBorder.borderWith = activeAttr.topBorder.borderWith
        finalAttr.rightBorder.borderStyle = activeAttr.rightBorder.borderStyle
        finalAttr.rightBorder.borderWith = activeAttr.rightBorder.borderWith
        finalAttr.bottomBorder.borderStyle = activeAttr.bottomBorder.borderStyle
        finalAttr.bottomBorder.borderWith = activeAttr.bottomBorder.borderWith
        finalAttr.leftBorder.borderStyle = activeAttr.leftBorder.borderStyle
        finalAttr.leftBorder.borderWith = activeAttr.leftBorder.borderWith
    }
}


val DecorationStyleNodeImpl = ImplFactory<DecorationStyleNode>()