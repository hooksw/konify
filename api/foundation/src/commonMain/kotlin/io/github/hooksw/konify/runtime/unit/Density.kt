/*
 * Copyright 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.hooksw.konify.runtime.unit;

import kotlin.math.roundToInt

/**
 * A density of the screen. Used for convert [Dp] to pixels.
 *
 * @param density The logical density of the display. This is a scaling factor for the [Dp] unit.
 * @param fontScale Current user preference for the scaling factor for fonts.
 */

fun Density(density: Float, fontScale: Float = 1f): Density =
    DensityImpl(density, fontScale)

private data class DensityImpl(
    override val density: Float,
    override val fontScale: Float
) : Density

/**
 * A density of the screen. Used for the conversions between pixels, [Dp], [Int] and [Sp].
 *
 */

interface Density {

    /**
     * The logical density of the display. This is a scaling factor for the [Dp] unit.
     */
    
    val density: Float

    /**
     * Current user preference for the scaling factor for fonts.
     */
    
    val fontScale: Float

    /**
     * Convert [Dp] to pixels. Pixels are used to paint to Canvas.
     */
    
    fun Dp.toPx(): Float = value * density

    /**
     * Convert [Dp] to [Int] by rounding
     */
    
    fun Dp.roundToPx(): Int {
        val px = toPx()
        return if (px.isInfinite()) Int.MAX_VALUE else px.roundToInt()
    }

    /**
     * Convert [Dp] to Sp. Sp is used for font size, etc.
     */
    
    fun Dp.toSp(): Sp = (value / fontScale).sp

    /**
     * Convert Sp to pixels. Pixels are used to paint to Canvas.
     * @throws IllegalStateException if Sp other than SP unit is specified.
     */
    
    fun Sp.toPx(): Float {
        return value * fontScale * density
    }

    /**
     * Convert Sp to [Int] by rounding
     */
    
    fun Sp.roundToPx(): Int = toPx().roundToInt()

    /**
     * Convert Sp to [Dp].
     * @throws IllegalStateException if Sp other than SP unit is specified.
     */
    
    fun Sp.toDp(): Dp {
        return Dp(value * fontScale)
    }

    /**
     * Convert an [Int] pixel value to [Dp].
     */
    
    fun Int.toDp(): Dp = (this / density).dp

    /**
     * Convert an [Int] pixel value to Sp.
     */
    
    fun Int.toSp(): Sp = (this / (fontScale * density)).sp

    /** Convert a [Float] pixel value to a Dp */
    
    fun Float.toDp(): Dp = (this / density).dp

    /** Convert a [Float] pixel value to a Sp */
    
    fun Float.toSp(): Sp = (this / (fontScale * density)).sp

}
