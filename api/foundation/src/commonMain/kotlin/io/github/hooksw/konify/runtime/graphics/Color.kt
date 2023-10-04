package io.github.hooksw.konify.runtime.graphics

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.jvm.JvmInline

fun Color(
    red: Int,
    green: Int,
    blue: Int,
    alpha: Int = 0xFF
): Color {
    return Color(packColor(red, green, blue, alpha))
}

fun Color(
    red: Float,
    green: Float,
    blue: Float,
    alpha: Float = 1.0f
): Color {
    return Color(packColor(red, green, blue, alpha))
}

fun Color(argb: Long): Color {
    return Color(argb.toULong())
}

@JvmInline
value class Color internal constructor(private val packedLong: ULong) {
    val red: Int
        get() = unpackRed(packedLong.toLong())

    val nRed: Float
        get() = red / 255.0f

    val green: Int
        get() = unpackGreen(packedLong.toLong())

    val nGreen: Float
        get() = green / 255.0f

    val blue: Int
        get() = unpackBlue(packedLong.toLong())

    val nBlue: Float
        get() = blue / 255.0f

    val alpha: Int
        get() = unpackAlpha(packedLong.toLong())

    val nAlpha: Float
        get() = alpha / 255.0f

    val rgb: Int
        get() = (packedLong.toLong() and 0xFFFFFF).toInt()

    val argb: Int
        get() = (packedLong.toLong() and 0xFFFFFFFF).toInt()

    operator fun component1(): Int = red

    operator fun component2(): Int = green

    operator fun component3(): Int = blue

    operator fun component4(): Int = alpha

    fun orElse(default: Color): Color {
        return if (isSpecified()) this else default
    }

    fun copy(
        alpha: Int = this.alpha,
        red: Int = this.red,
        green: Int = this.green,
        blue: Int = this.blue
    ): Color {
        return Color(red, green, blue, alpha)
    }

    fun copy(
        alpha: Float = nAlpha,
        red: Float = nRed,
        green: Float = nGreen,
        blue: Float = nBlue
    ): Color {
        return Color(red, green, blue, alpha)
    }

    override fun toString(): String {
        return if (isSpecified()) {
            "Color(red=$red, green=$green, blue=$blue, alpha=$alpha)"
        } else {
            "Color.Unspecified"
        }
    }

    companion object {
        val Black: Color = Color(0xFF000000)

        val DarkGray: Color = Color(0xFF444444)

        val Gray: Color = Color(0xFF888888)

        val LightGray: Color = Color(0xFFCCCCCC)

        val White: Color = Color(0xFFFFFFFF)

        val Red: Color = Color(0xFFFF0000)

        val Green: Color = Color(0xFF00FF00)

        val Blue: Color = Color(0xFF0000FF)

        val Yellow: Color = Color(0xFFFFFF00)

        val Magenta: Color = Color(0xFFFF00FF)

        val Cyan: Color = Color(0xFF00FFFF)

        val Transparent: Color = Color(0x00FFFFFF)

        val Unspecified: Color = Color(0x100FFFFFF)
    }
}

fun Color.isUnspecified(): Boolean {
    return this == Color.Unspecified
}

fun Color.isSpecified(): Boolean {
    return isUnspecified().not()
}

@OptIn(ExperimentalContracts::class)
inline fun Color.orElse(provider: () -> Color): Color {
    contract { callsInPlace(provider, InvocationKind.EXACTLY_ONCE) }

    return orElse(provider())
}

// -------- Internal --------

private fun packColor(red: Int, green: Int, blue: Int, alpha: Int = 0xFF): Long {
    return ((alpha and 0xFF shl 24) or
            (red and 0xFF shl 16) or
            (green and 0xFF shl 8) or
            (blue and 0xFF)).toLong()
}

private fun packColor(red: Float, green: Float, blue: Float, alpha: Float = 1.0f): Long {
    return packColor(
        red = (red * 255).toInt(),
        green = (green * 255).toInt(),
        blue = (blue * 255).toInt(),
        alpha = (alpha * 255).toInt()
    )
}

private fun unpackAlpha(packedLong: Long): Int {
    return packedLong.toInt() shr 24 and 0xFF
}

private fun unpackRed(packedLong: Long): Int {
    return packedLong.toInt() shr 16 and 0xFF
}

private fun unpackGreen(packedLong: Long): Int {
    return packedLong.toInt() shr 8 and 0xFF
}

private fun unpackBlue(packedLong: Long): Int {
    return packedLong.toInt() and 0xFF
}
