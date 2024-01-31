package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp

data class DecorationStyleAttr(
    val topLeftRadius: Dp = Dp.Unspecified,
    val topRightRadius: Dp = Dp.Unspecified,
    val bottomLeftRadius: Dp = Dp.Unspecified,
    val bottomRightRadius: Dp = Dp.Unspecified,
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

interface DecorationStyleNode : StyleNode<DecorationStyleAttr>

interface DecorationAttrHandler : AttrHandler<DecorationStyleAttr>

val DecorationStyleNodeImpl = ImplFactory<DecorationStyleNode>()