package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.graphics.Color

interface BackgroundStyleNode : StyleNode<BackgroundStyleAttr>

data class BackgroundStyleAttr(
    val color: Color = Color.Unspecified
)

interface BackgroundAttrHandler : AttrHandler<BackgroundStyleAttr>

val BackgroundStyleNodeImpl = ImplFactory<BackgroundStyleNode>()