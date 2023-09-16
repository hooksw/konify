package io.github.hooksw.konify.runtime.property

interface ViewEventProperty : Property {
    var onClick: () -> Unit
}