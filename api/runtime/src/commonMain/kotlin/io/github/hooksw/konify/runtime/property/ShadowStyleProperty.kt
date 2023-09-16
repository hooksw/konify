package io.github.hooksw.konify.runtime.property

interface ShadowStyleProperty:Property {

    var elevation: Float
}

internal expect class ShadowStylePropertyImpl():ShadowStyleProperty
