/*
 * Copyright 2018 The Android Open Source Project
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
@file:Suppress("NOTHING_TO_INLINE")

package io.github.hooksw.konify.runtime.unit

import kotlin.math.max
import kotlin.math.min

/**
 * Dimension value representing device-independent pixels (Sp). Component APIs specify their
 * dimensions such as line thickness in Sp with Sp objects. Hairline (1 pixel) thickness
 * may be specified with [Hairline], a dimension that take up no space. Sp are normally
 * defined using [Sp], which can be applied to [Int], [Double], and [Float].
 *
 * Drawing and Layout are done in pixels. To retrieve the pixel size of a Sp, use [Density.toPx]:
 *
 */

@kotlin.jvm.JvmInline
value class Sp(val value: Float) : Comparable<Sp> {
    /**
     * Add two [Sp]s together.
     */
    
    inline operator fun plus(other: Sp) =
        Sp(value = this.value + other.value)

    /**
     * Subtract a Sp from another one.
     */
    
    inline operator fun minus(other: Sp) =
        Sp(value = this.value - other.value)

    /**
     * This is the same as multiplying the Sp by -1.0.
     */
    
    inline operator fun unaryMinus() = Sp(-value)

    /**
     * Divide a Sp by a scalar.
     */
    
    inline operator fun div(other: Float): Sp =
        Sp(value = value / other)

    
    inline operator fun div(other: Int): Sp =
        Sp(value = value / other)

    /**
     * Divide by another Sp to get a scalar.
     */
    
    inline operator fun div(other: Sp): Float = value / other.value

    /**
     * Multiply a Sp by a scalar.
     */
    
    inline operator fun times(other: Float): Sp =
        Sp(value = value * other)

    
    inline operator fun times(other: Int): Sp =
        Sp(value = value * other)

    /**
     * Support comparing Dimensions with comparison operators.
     */
    
    override /* TODO: inline */ operator fun compareTo(other: Sp) = value.compareTo(other.value)

    
    override fun toString() = "$value.Sp"
}

/**
 * Create a [sp] using an [Int]:
 *     val left = 10
 *     val x = left.sp
 *     // -- or --
 *     val y = 10.sp
 */

inline val Int.sp: Sp get() = Sp(value = this.toFloat())

/**
 * Create a [sp] using a [Double]:
 *     val left = 10.0
 *     val x = left.sp
 *     // -- or --
 *     val y = 10.0.sp
 */

inline val Double.sp: Sp get() = Sp(value = this.toFloat())

/**
 * Create a [sp] using a [Float]:
 *     val left = 10f
 *     val x = left.sp
 *     // -- or --
 *     val y = 10f.sp
 */

inline val Float.sp: Sp get() = Sp(value = this)


inline operator fun Float.times(other: Sp) =
    Sp(this * other.value)


inline operator fun Double.times(other: Sp) =
    Sp(this.toFloat() * other.value)


inline operator fun Int.times(other: Sp) =
    Sp(this * other.value)


inline fun min(a: Sp, b: Sp): Sp = Sp(value = min(a.value, b.value))


inline fun max(a: Sp, b: Sp): Sp = Sp(value = max(a.value, b.value))

/**
 * Ensures that this value lies in the specified range [minimumValue]..[maximumValue].
 *
 * @return this value if it's in the range, or [minimumValue] if this value is less than
 * [minimumValue], or [maximumValue] if this value is greater than [maximumValue].
 */

inline fun Sp.coerceIn(minimumValue: Sp, maximumValue: Sp): Sp =
    Sp(value = value.coerceIn(minimumValue.value, maximumValue.value))

/**
 * Ensures that this value is not less than the specified [minimumValue].
 * @return this value if it's greater than or equal to the [minimumValue] or the
 * [minimumValue] otherwise.
 */

inline fun Sp.coerceAtLeast(minimumValue: Sp): Sp =
    Sp(value = value.coerceAtLeast(minimumValue.value))

/**
 * Ensures that this value is not greater than the specified [maximumValue].
 *
 * @return this value if it's less than or equal to the [maximumValue] or the
 * [maximumValue] otherwise.
 */

inline fun Sp.coerceAtMost(maximumValue: Sp): Sp =
    Sp(value = value.coerceAtMost(maximumValue.value))

/**
 *
 * Return `true` when it is finite or `false` when it is [Sp.Infinity]
 */

inline val Sp.isFinite: Boolean get() = value != Float.POSITIVE_INFINITY
