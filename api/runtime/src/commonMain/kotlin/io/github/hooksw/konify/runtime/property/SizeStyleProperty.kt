package io.github.hooksw.konify.runtime.property

interface SizeStyleProperty : Property {
    var width: Float
    var height: Float
    var maxWidth: Float
    var maxHeight: Float
    var minWidth: Float
    var minHeight: Float
}