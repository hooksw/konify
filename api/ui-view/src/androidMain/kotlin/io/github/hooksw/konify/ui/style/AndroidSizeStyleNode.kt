package io.github.hooksw.konify.ui.style

import android.view.View
import android.view.ViewGroup.LayoutParams
import io.github.hooksw.konify.foundation.modifier.SizeStyleNode
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.coerceIn
import io.github.hooksw.konify.foundation.unit.takeOrElse

class AndroidSizeStyleNode(override val style: Style) : SizeStyleNode() {

    override fun onUpdate(view: Any) {
        view as View
        with(style.density) {
            view.apply {
                layoutParams.width = finalAttr.width.intrinsicOr(
                    (finalAttr.width.coerceIn(
                        finalAttr.minWidth.takeOrElse { Dp(Float.MIN_VALUE) },
                        finalAttr.maxWidth.takeOrElse { Dp(Float.MAX_VALUE) }
                    )).roundToPx()
                )
                layoutParams.height = finalAttr.height.intrinsicOr(
                    (finalAttr.height.coerceIn(
                        finalAttr.minHeight.takeOrElse { Dp(Float.MIN_VALUE) },
                        finalAttr.maxHeight.takeOrElse { Dp(Float.MAX_VALUE) }
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