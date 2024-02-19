package io.github.hooksw.konify.foundation.modifier.style

import io.github.hooksw.konify.foundation.graphics.Brush
import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.graphics.SolidColor
import io.github.hooksw.konify.foundation.modifier.AttrElement
import io.github.hooksw.konify.foundation.modifier.Modifier

abstract class BackgroundElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<BackgroundStyleAttr>(modifier, update) {
    override fun BackgroundStyleAttr.updateFrom(other: BackgroundStyleAttr) {
        brush = other.brush
    }
}

data class BackgroundStyleAttr(
    var brush: Brush = SolidColor(Color.Unspecified),
)

interface BackgroundScope {
    fun color()
    fun linearGradient()
    fun radialGradient()
    fun sweepGradient()
}