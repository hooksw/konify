package io.github.hooksw.konify.ui.style

import android.view.View
import android.view.ViewGroup.LayoutParams
import io.github.hooksw.konify.foundation.UIElementHolder
import io.github.hooksw.konify.foundation.geometry.Match
import io.github.hooksw.konify.foundation.geometry.Wrap
import io.github.hooksw.konify.foundation.style.SizeStyleAttr
import io.github.hooksw.konify.runtime.style.SizeStyleNode
import io.github.hooksw.konify.foundation.style.Style
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.coerceIn
import io.github.hooksw.konify.foundation.unit.takeOrElse

class AndroidSizeStyleNode(override val style: Style) :
    io.github.hooksw.konify.runtime.style.SizeStyleNode<View> {
    override val attr: SizeStyleAttr = SizeStyleAttr()

    override fun update(nativeView: UIElementHolder<View>) {
        with(style.density) {
            nativeView.element.apply {
                layoutParams.width = attr.width.intrinsicOr(
                    (attr.width.coerceIn(
                        attr.minWidth.takeOrElse { Dp(Float.MIN_VALUE) },
                        attr.maxWidth.takeOrElse { Dp(Float.MAX_VALUE) }
                    )).roundToPx()
                )
                layoutParams.height = attr.height.intrinsicOr(
                    (attr.height.coerceIn(
                        attr.minHeight.takeOrElse { Dp(Float.MIN_VALUE) },
                        attr.maxHeight.takeOrElse { Dp(Float.MAX_VALUE) }
                    )).roundToPx()
                )
                requestLayout()
            }
        }
    }

    private fun Dp.intrinsicOr(other: Int) =
        when (this) {
            Match -> LayoutParams.MATCH_PARENT
            Wrap -> LayoutParams.WRAP_CONTENT
            else -> other
        }

}