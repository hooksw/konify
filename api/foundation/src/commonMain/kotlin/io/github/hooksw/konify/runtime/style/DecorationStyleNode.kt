package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.graphics.Color
import io.github.hooksw.konify.runtime.unit.Dp

internal data class DecorationStyleAttr(
    var backgroundColor: Color = Color.Unspecified,
    val radius: Radius = Radius(),
    val border: List<Border> = emptyList(),
)

enum class BorderStyle {
    Dotted, Dashed, Solid
}

internal data class Radius(
    var topLeft: Dp = Dp.Unspecified,
    var topRight: Dp = Dp.Unspecified,
    var bottomLeft: Dp = Dp.Unspecified,
    var bottomRight: Dp = Dp.Unspecified
)

internal data class Border(
    val direction: BorderDirection,
    var borderWith: Dp = Dp.Unspecified,
    var borderStyle: BorderStyle = BorderStyle.Solid
)

enum class BorderDirection {
    Top, Left, Right, Bottom
}

internal inline fun defaultBorder() = listOf(
    Border(BorderDirection.Top), Border(BorderDirection.Right),
    Border(BorderDirection.Bottom), Border(BorderDirection.Left)
)

internal expect class DecorationStyleNode : StyleNode<DecorationStyleAttr>
