package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp

interface PaddingStyleNode : StyleNode<PaddingStyleAttr>

data class PaddingStyleAttr(
    var top: Dp = Dp.Unspecified,
    var right: Dp = Dp.Unspecified,
    var bottom: Dp = Dp.Unspecified,
    var left: Dp = Dp.Unspecified,
)

interface PaddingAttrHandler : AttrHandler<PaddingStyleAttr>

val PaddingStyleNodeImpl = ImplFactory<PaddingStyleNode>()