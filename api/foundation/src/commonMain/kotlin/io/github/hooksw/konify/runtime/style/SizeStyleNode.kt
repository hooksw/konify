package io.github.hooksw.konify.runtime.style

import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.geometry.Match

data class SizeStyleAttr(
    var width: Dp = Match,
    var height: Dp = Match,
    var maxWidth: Dp = Dp.Unspecified,
    var maxHeight: Dp = Dp.Unspecified,
    var minWidth: Dp = Dp.Unspecified,
    var minHeight: Dp = Dp.Unspecified,
)

expect class SizeStyleNode : StyleNode<SizeStyleAttr>
