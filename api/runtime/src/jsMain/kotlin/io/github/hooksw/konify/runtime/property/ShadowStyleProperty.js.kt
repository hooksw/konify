package io.github.hooksw.konify.runtime.property

import io.github.hooksw.konify.runtime.platform.PlatformView

internal actual class ShadowStylePropertyImpl actual constructor() : ShadowStyleProperty {
    override var elevation: Float = 0f
        set(value) {
            field=value
        }

    override fun applyTo(platformView: PlatformView) {
        platformView.element.style.boxShadow="$elevation.px "
    }
}