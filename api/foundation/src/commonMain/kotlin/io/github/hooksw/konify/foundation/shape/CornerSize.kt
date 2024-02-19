package io.github.hooksw.konify.foundation.shape

import io.github.hooksw.konify.foundation.unit.Dp

interface CornerSize {

}

/**
 * Creates [CornerSize] with provided size.
 * @param size the corner size defined in [Dp].
 */

fun CornerSize(size: Dp): CornerSize = DpCornerSize(size)

class DpCornerSize(private val size: Dp) : CornerSize {
    override fun toString(): String = "CornerSize(size = ${size.value}.dp)"

}

/**
 * Creates [CornerSize] with provided size.
 * @param percent the corner size defined in percents of the shape's smaller side.
 * Can't be negative or larger then 100 percents.
 */

fun CornerSize(/*@IntRange(from = 0, to = 100)*/ percent: Int): CornerSize =
    PercentCornerSize(percent.toFloat())

/**
 * Creates [CornerSize] with provided size.
 * @param percent the corner size defined in float percents of the shape's smaller side.
 * Can't be negative or larger then 100 percents.
 */
class PercentCornerSize(
    /*@FloatRange(from = 0.0, to = 100.0)*/
    private val percent: Float
) : CornerSize {
    init {
        if (percent < 0 || percent > 100) {
            throw IllegalArgumentException("The percent should be in the range of [0, 100]")
        }
    }

    override fun toString(): String = "CornerSize(size = $percent%)"

}

/**
 * [CornerSize] always equals to zero.
 */

val ZeroCornerSize: CornerSize = object : CornerSize {

    override fun toString(): String = "ZeroCornerSize"

}
