package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.unit.Density

abstract class UiConfigurationElement(
    modifier: Modifier,
    update: () -> Unit
) : AttrElement<UiConfigurationAttr>(modifier, update) {
    override fun UiConfigurationAttr.updateFrom(other: UiConfigurationAttr) {
        layoutDirection = other.layoutDirection
        density = other.density
    }
}

enum class LayoutDirection {
    RTL, LTR
}

data class UiConfigurationAttr(
    var layoutDirection: LayoutDirection? = null,
    var density: Density? = null
)

interface UiConfigurationScope {
    var layoutDirection: LayoutDirection
    fun density(scale: Float, fontScale: Float)
}