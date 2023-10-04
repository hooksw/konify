package io.github.hooksw.konify.runtime.style

import android.view.ViewGroup.LayoutParams
import io.github.hooksw.konify.runtime.geometry.Match
import io.github.hooksw.konify.runtime.geometry.Wrap
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.unit.*

actual class SizeStyleNode : StyleNode<SizeStyleAttr> {
    override val attr: SizeStyleAttr = SizeStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            platformView.view.apply {
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