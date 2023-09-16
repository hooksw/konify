package io.github.hooksw.konify.runtime.property

import io.github.hooksw.konify.runtime.platform.PlatformView


interface Property{
    fun applyTo(platformView: PlatformView)
}