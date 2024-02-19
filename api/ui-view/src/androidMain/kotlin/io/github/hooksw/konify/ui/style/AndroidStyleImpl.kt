package io.github.hooksw.konify.ui.style

import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.core.view.updatePadding
import io.github.hooksw.konify.foundation.graphics.Color
import io.github.hooksw.konify.foundation.modifier.style.BorderStyle
import io.github.hooksw.konify.foundation.modifier.ConstraintHandler
import io.github.hooksw.konify.foundation.modifier.StyleImpl
import io.github.hooksw.konify.foundation.unit.Dp

open class AndroidStyleImpl : StyleImpl<View>() {

    private val scheduledRequestLayout=false
    private fun prepareToRequestLayout() {

    }

    private fun Dp.toPx() = with(density) { this@toPx.roundToPx() }
    override fun width(dp: Dp) {
        nativeElement.element.apply {
            layoutParams.width = dp.toPx()
        }
        prepareToRequestLayout()
    }

    override fun height(dp: Dp) {
        nativeElement.element.apply {
            layoutParams.height = dp.toPx()
        }
        prepareToRequestLayout()
    }

    override fun maxWidth(dp: Dp) {
        val view = nativeElement.element
        if (view is ConstraintHandler) {
            view.maxWidth(dp.toPx())
        } else {
            error("don't know how to handle this property")
        }
    }

    override fun maxHeight(dp: Dp) {
        val view = nativeElement.element
        if (view is ConstraintHandler) {
            view.maxHeight(dp.toPx())
        } else {
            error("don't know how to handle this property")
        }
    }

    override fun minWidth(dp: Dp) {
        val view = nativeElement.element
        if (view is ConstraintHandler) {
            view.maxWidth(dp.toPx())
        } else {
            view.minimumWidth = dp.toPx()
        }
    }

    override fun minHeight(dp: Dp) {
        val view = nativeElement.element
        if (view is ConstraintHandler) {
            view.maxWidth(dp.toPx())
        } else {
            view.minimumHeight = dp.toPx()
        }
    }

    override fun matchParent() {
        nativeElement.element.apply {
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
        prepareToRequestLayout()
    }

    override fun wrapContent() {
        nativeElement.element.apply {
            layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT
        }
        prepareToRequestLayout()
    }

    override fun paddingTop(dp: Dp) {
        nativeElement.element.updatePadding(top = dp.toPx())
    }

    override fun paddingBottom(dp: Dp) {
        nativeElement.element.updatePadding(bottom = dp.toPx())
    }

    override fun paddingLeft(dp: Dp) {
        nativeElement.element.updatePadding(left = dp.toPx())
    }

    override fun paddingRight(dp: Dp) {
        nativeElement.element.updatePadding(right = dp.toPx())
    }

    override fun marginTop(dp: Dp) {
        nativeElement.element.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            topMargin=dp.toPx()
        }
    }

    override fun marginBottom(dp: Dp) {
        nativeElement.element.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            bottomMargin=dp.toPx()
        }
    }

    override fun marginLeft(dp: Dp) {
        nativeElement.element.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            leftMargin=dp.toPx()
        }
    }

    override fun marginRight(dp: Dp) {
        nativeElement.element.updateLayoutParams<ViewGroup.MarginLayoutParams> {
            rightMargin=dp.toPx()
        }
    }

    override fun rotate(degree: Float) {
        TODO("Not yet implemented")
    }

    override fun rotateX(degree: Float) {
        TODO("Not yet implemented")
    }

    override fun rotateY(degree: Float) {
        TODO("Not yet implemented")
    }

    override fun scaleX(scale: Float) {
        TODO("Not yet implemented")
    }

    override fun scaleY(scale: Float) {
        TODO("Not yet implemented")
    }

    override fun translateX(dp: Dp) {
        TODO("Not yet implemented")
    }

    override fun translateY(dp: Dp) {
        TODO("Not yet implemented")
    }

    override fun borderRadius(
        applyToTopLeft: Boolean,
        applyToTopRight: Boolean,
        applyToBottomLeft: Boolean,
        applyToBottomRight: Boolean,
        dp: Dp
    ) {
        TODO("Not yet implemented")
    }

    override fun border(
        applyToTop: Boolean,
        applyToRight: Boolean,
        applyToBottom: Boolean,
        applyToLeft: Boolean,
        size: Dp,
        borderStyle: BorderStyle,
        color: Color
    ) {
        TODO("Not yet implemented")
    }

    override fun backgroundColor(color: Color) {
        TODO("Not yet implemented")
    }
}