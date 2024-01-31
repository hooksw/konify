package io.github.hooksw.konify.foundation.style

import io.github.hooksw.konify.foundation.ImplFactory
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.dp

interface SizeStyleNode : StyleNode<SizeStyleAttr>

data class SizeStyleAttr(
    var width: Dp = Match_Parent,
    var height: Dp = Wrap_Content,
    var maxWidth: Dp = Dp.Unspecified,
    var minWidth: Dp = Dp.Unspecified,
    var maxHeight: Dp = Dp.Unspecified,
    var minHeight: Dp = Dp.Unspecified,
)

interface SizeAttrHandler : AttrHandler<SizeStyleAttr>

val Match_Parent = (-1).dp
val Wrap_Content = (-2).dp

val SizeStyleNodeImpl = ImplFactory<SizeStyleNode>()