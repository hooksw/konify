package io.github.hooksw.konify.foundation.modifier

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.graphics.Color

abstract class BackgroundStyleNode : AttrElement<BackgroundStyleAttr>() {
    override val finalAttr: BackgroundStyleAttr = BackgroundStyleAttr()
    override val activeAttr: BackgroundStyleAttr = BackgroundStyleAttr()
    override fun update() {
        finalAttr.color = activeAttr.color
    }
}

data class BackgroundStyleAttr(
    var color: Color = Color.Unspecified
)


val BackgroundStyleNodeImpl = ImplFactory<BackgroundStyleNode>()