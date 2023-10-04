package io.github.hooksw.konify.runtime.style

import android.graphics.drawable.GradientDrawable
import io.github.hooksw.konify.runtime.graphics.Color
import io.github.hooksw.konify.runtime.graphics.orElse
import io.github.hooksw.konify.runtime.platform.PlatformView
import io.github.hooksw.konify.runtime.unit.Density
import io.github.hooksw.konify.runtime.unit.Dp
import io.github.hooksw.konify.runtime.unit.dp
import io.github.hooksw.konify.runtime.unit.takeOrElse

internal actual class DecorationStyleNode : StyleNode<DecorationStyleAttr> {
    override val attr: DecorationStyleAttr = DecorationStyleAttr()

    override fun update(density: Density, platformView: PlatformView) {
        with(density) {
            val view = platformView.view
            val blr = attr.radius.bottomLeft.or0().toPx()
            val brr = attr.radius.bottomRight.or0().toPx()
            val tlr = attr.radius.topLeft.or0().toPx()
            val trr = attr.radius.topRight.or0().toPx()
            if (view is DecorationHandler) {
                view.setBottomLeftRadius(blr)
                view.setBottomRightRadius(brr)
                view.setTopLeftRadius(tlr)
                view.setTopRightRadius(trr)
                view.setBorderWith(attr.borderWith)
                view.setBorderColor(attr.borderColor)
                view.setBackgroundColor(attr.backgroundColor)
                return
            }
            if (attr.backgroundColor == Color.Unspecified &&
                attr.borderWith == Dp.Unspecified
            ) {
                platformView.view.background = null
            } else {
                drawable.setColor(attr.backgroundColor.orElse { Color.Transparent }.argb)
                drawable.setStroke(
                    attr.borderWith.or0().roundToPx(),
                    attr.borderColor.orElse { Color.Black }.argb
                )
                val bw = attr.borderWith.roundToPx()
                if (bw > 0) {
                    view.setPadding(
                        view.paddingLeft + bw,
                        view.paddingTop + bw,
                        view.paddingRight + bw,
                        view.paddingBottom + bw
                    )
                }
                drawable.cornerRadii = floatArrayOf(tlr, tlr, trr, trr, brr, brr, blr, blr)
                view.background = drawable
            }
        }
    }

    private inline fun Dp.or0() = takeOrElse { 0.dp }
    private val drawable by lazy {
        GradientDrawable()
    }
}