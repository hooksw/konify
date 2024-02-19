package io.github.hooksw.konify.foundation.modifier.style

import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.modifier.AttrElement
import io.github.hooksw.konify.foundation.modifier.Modifier
import io.github.hooksw.konify.foundation.shape.CornerSize
import io.github.hooksw.konify.foundation.shape.ZeroCornerSize
import io.github.hooksw.konify.foundation.unit.Dp

data class BorderStyleAttr(
    var width: Dp,
    var color: Color,
    var style: BorderStyle,
    var topLeftRadius: CornerSize = ZeroCornerSize,
    var topRightRadius: CornerSize = ZeroCornerSize,
    var bottomLeftRadius: CornerSize = ZeroCornerSize,
    var bottomRightRadius: CornerSize = ZeroCornerSize
)

enum class BorderStyle {
    Dotted, Dashed, Solid
}

abstract class BorderStyleNode(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<BorderStyleAttr>(modifier, update) {
    override fun BorderStyleAttr.updateFrom(other: BorderStyleAttr) {
        width = other.width
        color = other.color
        style = other.style
        topLeftRadius = other.topLeftRadius
        topRightRadius = other.topRightRadius
        bottomLeftRadius = other.bottomLeftRadius
        bottomRightRadius = other.bottomRightRadius
    }
}

interface BorderScope {

    var width: Dp
    var color: Color
    var style: BorderStyle
    fun radius(
        topLeft: CornerSize,
        topRight: CornerSize,
        bottomLeft: CornerSize,
        bottomRight: CornerSize,
    )

    fun radius(all: CornerSize)
}