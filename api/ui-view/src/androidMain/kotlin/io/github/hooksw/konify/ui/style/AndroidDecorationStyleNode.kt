package io.github.hooksw.konify.ui.style

import android.graphics.drawable.GradientDrawable
import android.view.View
import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.graphics.orElse
import io.github.hooksw.konify.foundation.modifier.DecorationStyleNode
import io.github.hooksw.konify.foundation.modifier.Style
import io.github.hooksw.konify.foundation.unit.Dp
import io.github.hooksw.konify.foundation.unit.dp
import io.github.hooksw.konify.foundation.unit.takeOrElse

internal class AndroidDecorationStyleNode(override val style: Style) : DecorationStyleNode() {
    override fun onUpdate(view: Any) {
        with(style.density) {
            view as View
            val blr = finalAttr.radius.bottomLeft.or0().toPx()
            val brr = finalAttr.radius.bottomRight.or0().toPx()
            val tlr = finalAttr.radius.topLeft.or0().toPx()
            val trr = finalAttr.radius.topRight.or0().toPx()
            if (view is DecorationHandler) {
                view.setBottomLeftRadius(blr)
                view.setBottomRightRadius(brr)
                view.setTopLeftRadius(tlr)
                view.setTopRightRadius(trr)
                view.setBorderWith(finalAttr.borderWith)
                view.setBorderColor(finalAttr.borderColor)
                view.setBackgroundColor(finalAttr.backgroundColor)
                return
            }
            if (finalAttr.backgroundColor == Color.Unspecified &&
                finalAttr.borderWith == Dp.Unspecified
            ) {
                nativeView.element.background = null
            } else {
                drawable.setColor(finalAttr.backgroundColor.orElse { Color.Transparent }.argb)
                drawable.setStroke(
                    finalAttr.borderWith.or0().roundToPx(),
                    finalAttr.borderColor.orElse { Color.Black }.argb
                )
                val bw = finalAttr.borderWith.roundToPx()
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