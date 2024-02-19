package io.github.hooksw.konify.foundation.graphics

import io.github.hooksw.konify.foundation.geometry.Offset

interface Brush {
}

class SolidColor(val color: Color) : Brush

class LinearGradient internal constructor(
    private val colors: List<Color>,
    private val stops: List<Float>? = null,
    private val angel: Float,
    private val tileMode: TileMode = TileMode.Clamp
) : Brush

class RadialGradient internal constructor(
    private val colors: List<Color>,
    private val stops: List<Float>? = null,
    private val center: Offset,
    private val radius: Float,
    private val tileMode: TileMode = TileMode.Clamp
) : Brush

class SweepGradient internal constructor(
    private val center: Offset,
    private val colors: List<Color>,
    private val stops: List<Float>? = null
) : Brush